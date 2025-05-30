package org.mule.extension.vectors.internal.connection.model.huggingface;

import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Alias("huggingFace")
@DisplayName("Hugging Face")
public class HuggingFaceModelConnection implements BaseTextModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(HuggingFaceModelConnection.class);

  private static final String AUTH_ENDPOINT = "https://huggingface.co/api/whoami-v2";
  private static final String EMBEDDINGS_ENDPOINT = "https://router.huggingface.co/hf-inference/models/";
  private static final String PIPELINE_FEATURE_EXTRACTION_PATH = "/pipeline/feature-extraction";

  private final String apiKey;
  private final HttpClient httpClient;
  private final long timeout;

  public HuggingFaceModelConnection(String apiKey, long timeout, HttpClient httpClient) {
    this.apiKey = apiKey;
    this.timeout = timeout;
    this.httpClient = httpClient;
  }

  public String getApiKey() {
    return apiKey;
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_HUGGING_FACE;
  }

  @Override
  public void connect() throws ConnectionException {
    try {
      validateCredentials();
      LOGGER.debug("Connected to Hugging Face");
    } catch (Exception e) {
      throw new ConnectionException("Failed to connect to Hugging Face.", e);
    }
  }

  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public boolean isValid() {
    try {
      validateCredentials();
      return true;
    } catch (Exception e) {
      LOGGER.error("Failed to validate connection to Hugging Face.", e);
      return false;
    }
  }

  private void validateCredentials() throws ConnectionException {
    try {
      HttpRequest request = HttpRequest.builder()
          .method("GET")
          .uri(AUTH_ENDPOINT)
          .addHeader("Authorization", "Bearer " + apiKey)
          .build();

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(request, options);

      if (response.getStatusCode() == 401 || response.getStatusCode() == 403) {
        LOGGER.error("Authentication failed. Please check your credentials.");
        throw new ConnectionException("Invalid credentials");
      }

      if (response.getStatusCode() != 200) {
        String errorBody = new String(response.getEntity().getBytes());
        String errorMsg = String.format("Unexpected response code: %d - %s", 
            response.getStatusCode(), errorBody);
        LOGGER.error(errorMsg);
        throw new ConnectionException(errorMsg);
      }

    } catch (ConnectionException e) {
      throw e;
    } catch (Exception e) {
      LOGGER.error("Failed to validate credentials", e);
      throw new ConnectionException("Failed to validate credentials", e);
    }
  }

  @Override
  public Object generateTextEmbeddings(List<String> inputs, String modelName) {
    if(inputs == null || inputs.isEmpty()) {
      throw new IllegalArgumentException("Input list cannot be null or empty");
    }
    if(modelName == null || modelName.isEmpty()) {
      throw new IllegalArgumentException("Model name cannot be null or empty");
    }
    if (modelName == null || modelName.trim().isEmpty()) {
      String errorMsg = "Model name is null or empty. Please provide a valid Hugging Face model name.";
      LOGGER.error(errorMsg);
      throw new RuntimeException(errorMsg);
    }

    try {
      String url = String.format("%s%s%s", EMBEDDINGS_ENDPOINT, modelName, PIPELINE_FEATURE_EXTRACTION_PATH);
      LOGGER.info("Generating embeddings using model: {} at URL: {}", modelName, url);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("inputs", inputs);
      
      ObjectMapper mapper = new ObjectMapper();
      byte[] jsonBody = mapper.writeValueAsBytes(requestBody);

      HttpRequest request = HttpRequest.builder()
          .method("POST")
          .uri(url)
          .addHeader("Authorization", "Bearer " + apiKey)
          .addHeader("Content-Type", "application/json")
          .entity(new ByteArrayHttpEntity(jsonBody))
          .build();

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(request, options);

      if (response.getStatusCode() != 200) {
        String errorBody = new String(response.getEntity().getBytes());
        String errorMsg = String.format("Error generating embeddings. Status: %d - %s", 
            response.getStatusCode(), errorBody);
        LOGGER.error(errorMsg);
        throw new RuntimeException(errorMsg);
      }

      return new String(response.getEntity().getBytes());

    } catch (Exception e) {
      LOGGER.error("Error generating embeddings", e);
      throw new RuntimeException("Failed to generate embeddings", e);
    }
  }
}
