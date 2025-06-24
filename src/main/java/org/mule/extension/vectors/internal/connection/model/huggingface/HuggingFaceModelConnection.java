package org.mule.extension.vectors.internal.connection.model.huggingface;

import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Alias("huggingFace")
@DisplayName("Hugging Face")
public class HuggingFaceModelConnection implements BaseTextModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(HuggingFaceModelConnection.class);

  private static final String AUTH_ENDPOINT = "https://huggingface.co/api/whoami-v2";
  private static final String INFERENCE_ENDPOINT = "https://router.huggingface.co/hf-inference/models/";
  private static final String PIPELINE_FEATURE_EXTRACTION_PATH = "/pipeline/feature-extraction";

  private final String apiKey;
  private final HttpClient httpClient;
  private final long timeout;
  private final ObjectMapper objectMapper;

  public HuggingFaceModelConnection(String apiKey, long timeout, HttpClient httpClient) {
    this.apiKey = apiKey;
    this.timeout = timeout;
    this.httpClient = httpClient;
    this.objectMapper = new ObjectMapper();
  }

  public String getApiKey() {
    return apiKey;
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_HUGGING_FACE;
  }

  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public void validate() {
    try {
      validateCredentialsAsync().get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new ModuleException("Failed to validate connection to Hugging Face", MuleVectorsErrorType.INVALID_CONNECTION, e.getCause());
    }
  }

  private CompletableFuture<Void> validateCredentialsAsync() {
    return HttpRequestHelper.executeGetRequest(httpClient, AUTH_ENDPOINT, buildAuthHeaders(), (int) timeout)
        .thenAccept(response -> {
          int statusCode = response.getStatusCode();
          if (statusCode == 401 || statusCode == 403) {
            LOGGER.error("Authentication failed. Please check your credentials.");
            throw new ModuleException("Invalid credentials", MuleVectorsErrorType.INVALID_CONNECTION);
          }
          if (statusCode != 200) {
            handleErrorResponse(response, "Failed to validate credentials");
          }
        });
  }

  @Override
  public Object generateTextEmbeddings(List<String> inputs, String modelName) {
    if (inputs == null || inputs.isEmpty()) {
      throw new IllegalArgumentException("Input list cannot be null or empty");
    }
    if (modelName == null || modelName.trim().isEmpty()) {
      throw new IllegalArgumentException("Model name cannot be null or empty");
    }
    try {
      return generateTextEmbeddingsAsync(inputs, modelName).get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      if (e.getCause() instanceof ModuleException) {
        throw (ModuleException) e.getCause();
      }
      throw new ModuleException("Failed to generate embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }

  private CompletableFuture<String> generateTextEmbeddingsAsync(List<String> inputs, String modelName) {
    String url = buildInferenceUrl(modelName);
    try {
      byte[] body = buildEmbeddingsPayload(inputs);
      Map<String, String> headers = buildAuthHeaders();
      headers.put("Content-Type", "application/json");

      return HttpRequestHelper.executePostRequest(httpClient, url, headers, body, (int) timeout)
          .thenApply(this::handleEmbeddingResponse);
    } catch (JsonProcessingException e) {
      return CompletableFuture.failedFuture(new ModuleException("Failed to create request body", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
    }
  }

  private String handleEmbeddingResponse(HttpResponse response) {
    if (response.getStatusCode() != 200) {
      return handleErrorResponse(response, "Error generating embeddings");
    }
    try {
      return new String(response.getEntity().getBytes());
    } catch (IOException e) {
      throw new ModuleException("Failed to read embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }
  
  private String buildInferenceUrl(String modelName) {
    return INFERENCE_ENDPOINT + modelName + PIPELINE_FEATURE_EXTRACTION_PATH;
  }

  private byte[] buildEmbeddingsPayload(List<String> inputs) throws JsonProcessingException {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("inputs", inputs);
    return objectMapper.writeValueAsBytes(requestBody);
  }
  
  private Map<String, String> buildAuthHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + apiKey);
    return headers;
  }
  
  private String handleErrorResponse(HttpResponse response, String message) {
    try {
      String errorBody = new String(response.getEntity().getBytes());
      String errorMsg = String.format("%s. Status: %d - %s", message, response.getStatusCode(), errorBody);
      LOGGER.error(errorMsg);
      throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
    } catch (IOException e) {
      throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }
}
