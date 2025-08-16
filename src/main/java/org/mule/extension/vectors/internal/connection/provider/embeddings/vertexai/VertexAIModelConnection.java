/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.provider.embeddings.vertexai;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertexAIModelConnection implements BaseModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(VertexAIModelConnection.class);

  // OAuth Constants
  private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
  private static final String TOKEN_INTROSPECTION_URL = "https://oauth2.googleapis.com/tokeninfo";
  private static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
  private static final String AUDIENCE = "https://oauth2.googleapis.com/token";
  private static final String[] SCOPES = {"https://www.googleapis.com/auth/cloud-platform"};
  private static final String JWT_HEADER = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";

  // Security Constants
  private static final String RSA_ALGORITHM = "RSA";
  private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
  private static final String PRIVATE_KEY_BEGIN = "-----BEGIN PRIVATE KEY-----";
  private static final String PRIVATE_KEY_END = "-----END PRIVATE KEY-----";

  private final String projectId;
  private final String location;
  private final String clientEmail;
  private final String clientId;
  private final String privateKeyId;
  private final String privateKey;
  private final HttpClient httpClient;
  private final long timeout;
  private final int batchSize;
  private final ObjectMapper objectMapper;
  private final AtomicReference<String> accessToken = new AtomicReference<>();

  public VertexAIModelConnection(String projectId, String location, String clientEmail, String clientId, String privateKeyId,
                                 String privateKey, long timeout, int batchSize, HttpClient httpClient) {
    this.projectId = projectId;
    this.location = location;
    this.clientEmail = clientEmail;
    this.clientId = clientId;
    this.privateKeyId = privateKeyId;
    this.privateKey = privateKey;
    this.timeout = timeout;
    this.batchSize = batchSize;
    this.httpClient = httpClient;
    this.objectMapper = new ObjectMapper();
  }

  public String getLocation() {
    return this.location;
  }

  public HttpClient getHttpClient() {
    return this.httpClient;
  }

  public String getProjectId() {
    return this.projectId;
  }

  public String getPrivateKeyId() {
    return privateKeyId;
  }

  public String getPrivateKey() {
    return privateKey;
  }

  public String getClientId() {
    return clientId;
  }

  public ObjectMapper getObjectMapper() {
    return this.objectMapper;
  }

  public long getTotalTimeout() {
    return timeout;
  }

  public int getBatchSize() {
    return batchSize;
  }

  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public void validate() {
    try {
      getOrRefreshToken().get(); // Block for validation
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new ModuleException("Failed to validate connection to VertexAI.", MuleVectorsErrorType.INVALID_CONNECTION,
                                e.getCause());
    }
  }

  public CompletableFuture<String> getOrRefreshToken() {
    String currentToken = accessToken.get();
    if (currentToken != null) {
      return validateAccessTokenAsync(currentToken).thenCompose(isValid -> {
        if (isValid) {
          return CompletableFuture.completedFuture(currentToken);
        } else {
          return refreshAccessTokenAsync();
        }
      });
    }
    return refreshAccessTokenAsync();
  }

  CompletableFuture<Boolean> validateAccessTokenAsync(String token) {
    String introspectionUrl = TOKEN_INTROSPECTION_URL + "?access_token=" + token;
    return HttpRequestHelper.executeGetRequest(httpClient, introspectionUrl, null, (int) timeout)
        .thenApply(response -> {
          if (response.getStatusCode() == 200) {
            try {
              Map<String, Object> tokenInfo = objectMapper.readValue(response.getEntity().getBytes(), Map.class);
              return tokenInfo.containsKey("expires_in") && ((Number) tokenInfo.get("expires_in")).intValue() > 0;
            } catch (IOException e) {
              LOGGER.error("Failed to parse token info response", e);
              return false;
            }
          }
          return false;
        }).exceptionally(ex -> {
          LOGGER.error("Error validating access token", ex);
          return false;
        });
  }

  CompletableFuture<String> refreshAccessTokenAsync() {
    try {
      String jwt = createJwt();
      String body = "grant_type=" + GRANT_TYPE + "&assertion=" + jwt;
      Map<String, String> headers = Map.of("Content-Type", "application/x-www-form-urlencoded");

      return HttpRequestHelper
          .executePostRequest(httpClient, TOKEN_URL, headers, body.getBytes(StandardCharsets.UTF_8), (int) timeout)
          .thenApply(response -> {
            if (response.getStatusCode() != 200) {
              handleErrorResponse(response, "Error getting access token");
              return null; // Should be handled by handleErrorResponse throwing an exception
            }
            try {
              Map<String, Object> tokenResponse = objectMapper.readValue(response.getEntity().getBytes(), Map.class);
              String newAccessToken = (String) tokenResponse.get("access_token");
              accessToken.set(newAccessToken);
              return newAccessToken;
            } catch (IOException e) {
              throw new ModuleException("Failed to parse access token response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
            }
          });
    } catch (Exception e) {
      return CompletableFuture
          .failedFuture(new ModuleException("Failed to create JWT for access token", MuleVectorsErrorType.INVALID_CONNECTION, e));
    }
  }

  String createJwt() {
    try {
      RSAPrivateKey rsaPrivateKey = parsePrivateKey(this.privateKey);
      Instant now = Instant.now();
      long iat = now.getEpochSecond();
      long exp = now.plusSeconds(3600).getEpochSecond();

      String header = base64UrlEncode(JWT_HEADER.getBytes(StandardCharsets.UTF_8));
      String payload = base64UrlEncode(String.format("{\"iss\":\"%s\",\"scope\":\"%s\",\"aud\":\"%s\",\"exp\":%d,\"iat\":%d}",
                                                     clientEmail, String.join(" ", SCOPES), AUDIENCE, exp, iat)
          .getBytes(StandardCharsets.UTF_8));

      String signatureBase = header + "." + payload;
      byte[] signatureBytes = signWithRSA(signatureBase.getBytes(StandardCharsets.UTF_8), rsaPrivateKey);
      String signature = base64UrlEncode(signatureBytes);

      return signatureBase + "." + signature;
    } catch (Exception e) {
      throw new ModuleException("Failed to create JWT for access token", MuleVectorsErrorType.INVALID_CONNECTION, e);
    }
  }

  private RSAPrivateKey parsePrivateKey(String pem) {
    try {
      pem = pem.replace(PRIVATE_KEY_BEGIN, "").replace(PRIVATE_KEY_END, "").replaceAll("\\s+", "");
      byte[] pkcs8Bytes = Base64.getDecoder().decode(pem);
      KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
      return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8Bytes));
    } catch (Exception e) {
      throw new ModuleException("Failed to parse private key: " + e.getMessage(), MuleVectorsErrorType.INVALID_CONNECTION, e);
    }
  }

  private byte[] signWithRSA(byte[] data, PrivateKey rsaPrivateKey) {
    try {
      Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
      sig.initSign(rsaPrivateKey);
      sig.update(data);
      return sig.sign();
    } catch (Exception e) {
      throw new ModuleException("Failed to sign JWT with RSA", MuleVectorsErrorType.INVALID_CONNECTION, e);
    }
  }

  private String base64UrlEncode(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String handleErrorResponse(HttpResponse response, String message) {
    try {
      String errorBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
      String errorMsg = String.format("%s. Status: %d - %s", message, response.getStatusCode(), errorBody);
      LOGGER.error(errorMsg);
      throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
    } catch (IOException e) {
      throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_VERTEX_AI;
  }
}
