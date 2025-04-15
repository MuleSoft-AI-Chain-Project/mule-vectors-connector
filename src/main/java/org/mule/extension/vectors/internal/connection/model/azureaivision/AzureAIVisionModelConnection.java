package org.mule.extension.vectors.internal.connection.model.azureaivision;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AzureAIVisionModelConnection implements BaseModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureAIVisionModelConnection.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final String endpoint;
  private final String apiKey;
  private final String apiVersion;
  private final long timeout;
  private final HttpClient httpClient;

  public AzureAIVisionModelConnection(String endpoint, String apiKey, String apiVersion, long timeout, HttpClient httpClient) {
    this.endpoint = endpoint;
    this.apiKey = apiKey;
    this.apiVersion = apiVersion;
    this.timeout = timeout;
    this.httpClient = httpClient;
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_AZURE_AI_VISION;
  }

  @Override
  public void connect() throws ConnectionException {
    try {
      // Test connection by getting models
      getModels();
      LOGGER.debug("Connected to Azure AI Vision");
    } catch (Exception e) {
      throw new ConnectionException("Failed to connect to Azure AI Vision", e);
    }
  }

  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public boolean isValid() {
    try {
      getModels();
      return true;
    } catch (Exception e) {
      LOGGER.error("Failed to validate connection to Azure AI Vision", e);
      return false;
    }
  }

  public float[] embedText(String text, String modelName) {
    LOGGER.debug(String.format("Embedding text: %s, Model name: %s", text, modelName));
    try {
      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .method("POST")
          .uri(endpoint + "/computervision/retrieval:vectorizeText")
          .addQueryParam("api-version", apiVersion)
          .addQueryParam("model-version", modelName)
          .addHeader("Content-Type", "application/json")
          .addHeader("Ocp-Apim-Subscription-Key", apiKey);

      String jsonBody = OBJECT_MAPPER.writeValueAsString(new TextEmbeddingRequest(text));
      requestBuilder.entity(new ByteArrayHttpEntity(jsonBody.getBytes()));

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)  // Cast long to int
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(requestBuilder.build(), options);
      validateResponse(response);

      TextEmbeddingResponse embeddingResponse = OBJECT_MAPPER.readValue(
          new String(response.getEntity().getBytes()), 
          TextEmbeddingResponse.class
      );
      return embeddingResponse.getVector();

    } catch (Exception e) {
      throw new RuntimeException("Failed to embed text", e);
    }
  }

  public float[] embedImage(byte[] imageBytes, String modelName) {
    LOGGER.debug(String.format("Embedding image, Model name: %s", modelName));
    try {
      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .method("POST")
          .uri(endpoint + "/computervision/retrieval:vectorizeImage")
          .addQueryParam("api-version", apiVersion)
          .addQueryParam("model-version", modelName)
          .addHeader("Content-Type", "application/octet-stream")
          .addHeader("Ocp-Apim-Subscription-Key", apiKey)
          .entity(new ByteArrayHttpEntity(imageBytes));

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)  // Cast long to int
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(requestBuilder.build(), options);
      validateResponse(response);

      TextEmbeddingResponse embeddingResponse = OBJECT_MAPPER.readValue(
          new String(response.getEntity().getBytes()), 
          TextEmbeddingResponse.class
      );
      return embeddingResponse.getVector();

    } catch (Exception e) {
      throw new RuntimeException("Failed to embed image", e);
    }
  }

  private void getModels() throws IOException, TimeoutException {
    HttpRequestBuilder requestBuilder = HttpRequest.builder()
        .method("GET")
        .uri(endpoint + "/computervision/models")
        .addQueryParam("api-version", apiVersion)
        .addHeader("Ocp-Apim-Subscription-Key", apiKey);

    HttpRequestOptions options = HttpRequestOptions.builder()
        .responseTimeout((int)timeout)  // Cast long to int
        .followsRedirect(false)
        .build();

    HttpResponse response = httpClient.send(requestBuilder.build(), options);
    validateResponse(response);
  }

  private void validateResponse(HttpResponse response) {
    try {
      if (response.getStatusCode() != 200) {
        String errorBody = new String(response.getEntity().getBytes());
        throw new RuntimeException(String.format("Azure AI Vision API error (HTTP %d): %s", 
            response.getStatusCode(), errorBody));
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read error response", e);
    }
  }

  // Inner classes for JSON serialization/deserialization
  private static class TextEmbeddingRequest {
    @JsonProperty("text")
    private final String text;

    public TextEmbeddingRequest(@JsonProperty("text") String text) {
      this.text = text;
    }

    @JsonProperty("text")
    public String getText() {
      return text;
    }
  }

  private static class TextEmbeddingResponse {
    @JsonProperty("vector")
    private float[] vector;

    @JsonProperty("modelVersion")
    private String modelVersion;

    @JsonProperty("vector")
    public float[] getVector() {
      return vector;
    }

    @JsonProperty("vector")
    public void setVector(float[] vector) {
      this.vector = vector;
    }

    @JsonProperty("modelVersion")
    public String getModelVersion() {
      return modelVersion;
    }

    @JsonProperty("modelVersion") 
    public void setModelVersion(String modelVersion) {
      this.modelVersion = modelVersion;
    }
  }
}
