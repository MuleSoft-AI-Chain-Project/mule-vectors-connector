/*
 * Copyright (c) 2024, Salesforce, Inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package org.mule.extension.vectors.internal.connection.model.vertexai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.extension.vectors.internal.connection.model.BaseImageModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class VertexAIModelConnection implements BaseTextModelConnection, BaseImageModelConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertexAIModelConnection.class);

    // OAuth Constants
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String TOKEN_INTROSPECTION_URL = "https://oauth2.googleapis.com/tokeninfo";
    private static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final String AUDIENCE = "https://oauth2.googleapis.com/token";
    private static final String[] SCOPES = {"https://www.googleapis.com/auth/cloud-platform"};
    private static final String JWT_HEADER = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";

    // Vertex AI Constants
    private static final String VERTEX_AI_ENDPOINT_FORMAT = "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:predict";

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

    public VertexAIModelConnection(String projectId, String location, String clientEmail, String clientId, String privateKeyId, String privateKey, long timeout, int batchSize, HttpClient httpClient) {
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
            throw new ModuleException("Failed to validate connection to VertexAI.", MuleVectorsErrorType.INVALID_CONNECTION, e.getCause());
        }
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

    public long getTotalTimeout() {
        return timeout;
    }

    public int getBatchSize() {
        return batchSize;
    }

    private CompletableFuture<String> getOrRefreshToken() {
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

    private CompletableFuture<Boolean> validateAccessTokenAsync(String token) {
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

    private CompletableFuture<String> refreshAccessTokenAsync() {
        try {
            String jwt = createJwt();
            String body = "grant_type=" + GRANT_TYPE + "&assertion=" + jwt;
            Map<String, String> headers = Map.of("Content-Type", "application/x-www-form-urlencoded");

            return HttpRequestHelper.executePostRequest(httpClient, TOKEN_URL, headers, body.getBytes(StandardCharsets.UTF_8), (int) timeout)
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
            return CompletableFuture.failedFuture(new ModuleException("Failed to create JWT for access token", MuleVectorsErrorType.INVALID_CONNECTION, e));
        }
    }

    @Override
    public Object generateTextEmbeddings(List<String> inputs, String modelName) {
        return executeEmbeddingRequest(buildTextPayload(inputs, modelName), modelName);
    }

    @Override
    public Object generateImageEmbeddings(List<byte[]> imageBytesList, String modelName) {
        return executeEmbeddingRequest(buildImagePayload(imageBytesList), modelName);
    }
    
    private String executeEmbeddingRequest(String payload, String modelName) {
        try {
            return getOrRefreshToken()
                    .thenCompose(token -> {
                        String url = String.format(VERTEX_AI_ENDPOINT_FORMAT, location, projectId, location, modelName);
                        Map<String, String> headers = Map.of("Authorization", "Bearer " + token, "Content-Type", "application/json");
                        return HttpRequestHelper.executePostRequest(httpClient, url, headers, payload.getBytes(StandardCharsets.UTF_8), (int) timeout);
                    })
                    .thenApply(this::handleEmbeddingResponse).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            if (e.getCause() instanceof ModuleException) {
                throw (ModuleException) e.getCause();
            }
            throw new ModuleException("Failed to generate embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private String handleEmbeddingResponse(HttpResponse response) {
        if (response.getStatusCode() != 200) {
            return handleErrorResponse(response, "Failed to generate embeddings");
        }
        try {
            return new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ModuleException("Failed to read embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private String buildTextPayload(List<String> inputs, String modelName) {
        List<Map<String, String>> instances = inputs.stream()
                .map(input -> Map.of(modelName.startsWith("text") ? "content" : "text", input))
                .collect(Collectors.toList());
        return buildPayload(instances);
    }
    
    private String buildImagePayload(List<byte[]> imageBytesList) {
        List<Map<String, Map<String, String>>> instances = imageBytesList.stream()
                .map(imageBytes -> {
                    String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
                    return Map.of("image", Map.of("bytesBase64Encoded", encodedImage));
                })
                .collect(Collectors.toList());
        return buildPayload(instances);
    }

    private String buildPayload(List<?> instances) {
        try {
            return objectMapper.writeValueAsString(Map.of("instances", instances));
        } catch (JsonProcessingException e) {
            throw new ModuleException("Failed to build request payload", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e);
        }
    }
    
    private String createJwt() throws Exception {
        RSAPrivateKey rsaPrivateKey = parsePrivateKey(this.privateKey);
        Instant now = Instant.now();
        long iat = now.getEpochSecond();
        long exp = now.plusSeconds(3600).getEpochSecond();

        String header = base64UrlEncode(JWT_HEADER.getBytes(StandardCharsets.UTF_8));
        String payload = base64UrlEncode(String.format("{\"iss\":\"%s\",\"scope\":\"%s\",\"aud\":\"%s\",\"exp\":%d,\"iat\":%d}",
                clientEmail, String.join(" ", SCOPES), AUDIENCE, exp, iat).getBytes(StandardCharsets.UTF_8));

        String signatureBase = header + "." + payload;
        byte[] signatureBytes = signWithRSA(signatureBase.getBytes(StandardCharsets.UTF_8), rsaPrivateKey);
        String signature = base64UrlEncode(signatureBytes);

        return signatureBase + "." + signature;
    }

    private RSAPrivateKey parsePrivateKey(String pem) throws Exception {
        pem = pem.replace(PRIVATE_KEY_BEGIN, "").replace(PRIVATE_KEY_END, "").replaceAll("\\s+", "");
        byte[] pkcs8Bytes = Base64.getDecoder().decode(pem);
        KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
        return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8Bytes));
    }

    private byte[] signWithRSA(byte[] data, PrivateKey rsaPrivateKey) throws Exception {
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initSign(rsaPrivateKey);
        sig.update(data);
        return sig.sign();
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
