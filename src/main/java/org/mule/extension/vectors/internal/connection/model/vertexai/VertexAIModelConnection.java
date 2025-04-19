package org.mule.extension.vectors.internal.connection.model.vertexai;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.BaseImageModelConnection;
import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;

import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

public class VertexAIModelConnection implements BaseTextModelConnection, BaseImageModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(VertexAIModelConnection.class);

  private static final String GRANT_TYPE = "grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=";
  private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
  private static final String TOKEN_INTROSPECTION_URL = "https://oauth2.googleapis.com/tokeninfo";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";
  private static final String PRIVATE_KEY_BEGIN = "-----BEGIN PRIVATE KEY-----";
  private static final String PRIVATE_KEY_END = "-----END PRIVATE KEY-----";
  private static final String RSA_ALGORITHM = "RSA";
  private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
  private static final String JWT_HEADER = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
  private static final String AUDIENCE = "https://oauth2.googleapis.com/token";
  private static final String[] SCOPES = {
      "https://www.googleapis.com/auth/cloud-platform",
      "https://www.googleapis.com/auth/cloud-platform.read-only"
  };
  private static final String VERTEX_AI_ENDPOINT_FORMAT = "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:predict";

  private String projectId;
  private String location;
  private String clientEmail;
  private String clientId;
  private String privateKeyId;
  private String privateKey;  
  private final HttpClient httpClient;
  private final long timeout;

  private String accessToken;

  public VertexAIModelConnection(String projectId, String location, String clientEmail, String clientId, String privateKeyId,
                                 String privateKey, long timeout, HttpClient httpClient) {
    this.projectId = projectId;
    this.location = location;
    this.clientEmail = clientEmail;
    this.clientId = clientId;
    this.privateKeyId = privateKeyId;
    this.privateKey = privateKey;
    this.timeout = timeout;
    this.httpClient = httpClient;
  }

  @Override
  public void connect() throws ConnectionException {

    try {

      this.accessToken = getAccessToken();
      if (this.accessToken == null) {
        throw new ConnectionException("Failed to connect to Vertex AI");
      }
    } catch (ConnectionException ce) {
      throw ce;
    } catch (Exception e) {
      throw new ConnectionException("Failed to connect to Vertex AI.", e);
    }
  }

  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public boolean isValid() {
    try {

      return validateAccessToken(this.accessToken);
    } catch (Exception e) {
      return false;
    }
  }

  private String getAccessToken() throws ConnectionException{

    try {
        
      // Convert PEM to RSAPrivateKey
      RSAPrivateKey privateKey = parsePrivateKey(this.privateKey);

      // Build JWT
      Instant now = Instant.now();
      long iat = now.getEpochSecond();
      long exp = now.plusSeconds(3600).getEpochSecond();

      String header = base64UrlEncode(JWT_HEADER);
      String payload = base64UrlEncode(String.format("""
              {
                "iss":"%s",
                "scope":"%s",
                "aud":"%s",
                "exp":%d,
                "iat":%d
              }
          """, clientEmail, String.join(" ", SCOPES), AUDIENCE, exp, iat));

      String signatureBase = header + "." + payload;
      String signature = base64UrlEncode(signWithRSA(signatureBase.getBytes(StandardCharsets.UTF_8), privateKey));
      String jwt = signatureBase + "." + signature;

      // Exchange JWT for access token
      String form = GRANT_TYPE + jwt;

      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .uri(TOKEN_URL)
          .method("POST")
          .addHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE)
          .entity(new ByteArrayHttpEntity(form.getBytes(StandardCharsets.UTF_8)));

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(requestBuilder.build(), options);
      
      if (response.getStatusCode() != 200) {
        String errorBody = new String(response.getEntity().getBytes());
        String errorMsg = String.format("Error getting access token. Status: %d - %s", 
            response.getStatusCode(), errorBody);
        LOGGER.error(errorMsg);
        throw new ConnectionException(errorMsg);
      }

      String responseBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
      JSONObject jsonResponse = new JSONObject(responseBody);
      return jsonResponse.getString("access_token");

    } catch (ConnectionException re) {

      throw re;

    } catch (Exception e) { 

      LOGGER.error("Error getting access token", e);
      throw new ConnectionException("Error getting access token", e);
    }
  }

  private RSAPrivateKey parsePrivateKey(String pem) throws Exception {
    pem = pem.replace(PRIVATE_KEY_BEGIN, "")
        .replace(PRIVATE_KEY_END, "")
        .replaceAll("\\s+", "");
    byte[] pkcs8Bytes = Base64.getDecoder().decode(pem);
    KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
    return (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8Bytes));
  }

  private byte[] signWithRSA(byte[] data, PrivateKey privateKey) throws Exception {
    Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
    sig.initSign(privateKey);
    sig.update(data);
    return sig.sign();
  }

  private String base64UrlEncode(String str) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(str.getBytes(StandardCharsets.UTF_8));
  }

  private String base64UrlEncode(byte[] bytes) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public boolean validateAccessToken(String accessToken) {
    try {
      String introspectionUrl = TOKEN_INTROSPECTION_URL + "?access_token=" + accessToken;

      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .uri(introspectionUrl)
          .method("GET");

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(requestBuilder.build(), options);

      if (response.getStatusCode() == 200) {
        String responseBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
        JSONObject jsonResponse = new JSONObject(responseBody);
        return jsonResponse.has("expires_in") && jsonResponse.getInt("expires_in") > 0;
      }
      return false;
    } catch (Exception e) {
      LOGGER.error("Error validating access token", e);
      return false;
    }
  }

  @Override
  public Object generateEmbeddings(List<String> inputs, String modelName) {
      
    // Prepare the input data
    JSONArray instances = new JSONArray();
    for (String input : inputs) {
      JSONObject instance = new JSONObject();
      instance.put(modelName.startsWith("text") ? "content" : "text", input);
      instances.put(instance);
    }

    JSONObject requestBodyJson = new JSONObject();
    requestBodyJson.put("instances", instances);
    String requestBody = requestBodyJson.toString();

    return generateEmbeddings(requestBody, modelName);
  }

  @Override
  public Object generateEmbeddings(byte[] imageBytes, String modelName) {

    return generateEmbeddings(null, imageBytes, modelName);
  }

  @Override
  public Object generateEmbeddings(String text, byte[] imageBytes, String modelName) {
    
    // Convert the image to Base64
    byte[] imageData = Base64.getEncoder().encode(imageBytes);
    String encodedImage = new String(imageData, StandardCharsets.UTF_8);
    JSONObject jsonImage = new JSONObject();
    jsonImage.put("bytesBase64Encoded", encodedImage);

    JSONObject instance = new JSONObject();
    if(text != null && !text.isEmpty()) instance.put("text", text);
    instance.put("image", jsonImage);

    JSONArray instances = new JSONArray();
    instances.put(instance);

    JSONObject requestBodyJson = new JSONObject();
    requestBodyJson.put("instances", instances);
    String requestBody = requestBodyJson.toString();

    return generateEmbeddings(requestBody, modelName);
  }

  private String generateEmbeddings(String requestBody, String modelName) {
    try {
      // Define the Vertex AI endpoint with the projectId
      String vertexAiEndpoint = String.format(
          VERTEX_AI_ENDPOINT_FORMAT,
          location,
          projectId,
          location,
          modelName
      );

      // Create the HTTP request using Mule's HttpClient
      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .uri(vertexAiEndpoint)
          .method("POST")
          .addHeader("Authorization", "Bearer " + accessToken)
          .addHeader("Content-Type", "application/json")
          .entity(new ByteArrayHttpEntity(requestBody.getBytes(StandardCharsets.UTF_8)));

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(requestBuilder.build(), options);

      // Check the response status
      if (response.getStatusCode() == 200) {
        return new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
      } else {
        throw new RuntimeException("Failed to generate embeddings: " + new String(response.getEntity().getBytes()));
      }
    } catch (Exception e) {
      throw new RuntimeException("Error generating embeddings", e);
    }
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_VERTEX_AI;
  }
}
