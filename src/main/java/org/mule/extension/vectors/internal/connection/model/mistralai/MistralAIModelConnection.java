package org.mule.extension.vectors.internal.connection.model.mistralai;

import org.json.JSONObject;
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

import java.util.List;

@Alias("mistralAI")
@DisplayName("Mistral AI")
public class MistralAIModelConnection implements BaseTextModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(MistralAIModelConnection.class);
  private static final String MODELS_ENDPOINT = "https://api.mistral.ai/v1/models";
  private static final String EMBEDDINGS_ENDPOINT = "https://api.mistral.ai/v1/embeddings";

  private final String apiKey;
  private final HttpClient httpClient;
  private final long timeout;

  public MistralAIModelConnection(String apiKey, long timeout, HttpClient httpClient) {
    this.apiKey = apiKey;
    this.timeout = timeout;
    this.httpClient = httpClient;
  }

  public String getApiKey() {
    return apiKey;
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_MISTRAL_AI;
  }

  @Override
  public void connect() throws ConnectionException {
    try {
      validateModels();
      LOGGER.debug("Connected to Mistral AI");
    } catch (Exception e) {
      throw new ConnectionException("Failed to connect to Mistral AI.", e);
    }
  }

  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public boolean isValid() {
    try {
      validateModels();
      return true;
    } catch (Exception e) {
      LOGGER.error("Failed to validate connection to Mistral AI.", e);
      return false;
    }
  }

  private void validateModels() throws ConnectionException {
    try {
      HttpRequest request = HttpRequest.builder()
          .method("GET")
          .uri(MODELS_ENDPOINT)
          .addHeader("Authorization", "Bearer " + apiKey)
          .addHeader("Content-Type", "application/json")
          .addHeader("Accept", "application/json")
          .build();

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(request, options);

      if (response.getStatusCode() != 200) {
        String errorBody = new String(response.getEntity().getBytes());
        String errorMsg = String.format("Error (HTTP %d): %s", response.getStatusCode(), errorBody);
        LOGGER.error(errorMsg);
        throw new ConnectionException("Failed to connect to Mistral AI. " + errorMsg);
      }
    } catch (ConnectionException e) {
      throw e;
    } catch (Exception e) {
      LOGGER.error("Failed to connect to Mistral AI", e);
      throw new ConnectionException("Failed to connect to Mistral AI", e);
    }
  }

  public Object generateTextEmbeddings(List<String> inputs, String modelName) {
    if(inputs == null || inputs.isEmpty()) {
      throw new IllegalArgumentException("Input list cannot be null or empty");  
    }
    if(modelName == null || modelName.isEmpty()) {
      throw new IllegalArgumentException("Model name cannot be null or empty");
    }

    try {
      JSONObject requestBody = new JSONObject();
      requestBody.put("model", modelName);
      requestBody.put("input", inputs);

      HttpRequest request = HttpRequest.builder()
          .method("POST")
          .uri(EMBEDDINGS_ENDPOINT)
          .addHeader("Authorization", "Bearer " + apiKey)
          .addHeader("Content-Type", "application/json")
          .entity(new ByteArrayHttpEntity(requestBody.toString().getBytes()))
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
