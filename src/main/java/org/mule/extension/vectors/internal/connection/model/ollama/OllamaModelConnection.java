package org.mule.extension.vectors.internal.connection.model.ollama;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Alias("ollama")
@DisplayName("Ollama")
public class OllamaModelConnection implements BaseTextModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(OllamaModelConnection.class);

  private final String baseUrl;
  private final HttpClient httpClient;
  private final long timeout;
  private static final String MODELS_ENDPOINT = "/api/tags";
  private static final String EMBEDDINGS_ENDPOINT = "/api/embeddings";

  public OllamaModelConnection(String baseUrl, long timeout, HttpClient httpClient) {
    this.baseUrl = baseUrl;
    this.timeout = timeout;
    this.httpClient = httpClient;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public long getTimeout() { return timeout; }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_OLLAMA;
  }

  @Override
  public void connect() throws ConnectionException {
    try {
      getModels();
      LOGGER.debug("Connected to Ollama");
    } catch (Exception e) {
      throw new ConnectionException("Failed to connect to Ollama.", e);
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
      LOGGER.error("Failed to validate connection to Ollama.", e);
      return false;
    }
  }

  private void getModels() throws ConnectionException {
    try {
      HttpRequest request = HttpRequest.builder()
          .method("GET")
          .uri(baseUrl + MODELS_ENDPOINT)
          .build();

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(request, options);

      if (response.getStatusCode() != 200) {
        String errorBody = new String(response.getEntity().getBytes());
        String errorMsg = String.format("Error getting models. Status: %d - %s", 
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

  public Object generateEmbeddings(List<String> inputs, String modelName) {
    if(inputs == null || inputs.isEmpty()) {
      throw new IllegalArgumentException("Input list cannot be null or empty");
    }
    if(inputs.size() != 1) {
      throw new IllegalArgumentException("Ollama embeddings API accepts only one input at a time");
    }
    if(modelName == null || modelName.isEmpty()) {
      throw new IllegalArgumentException("Model name cannot be null or empty");
    }

    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", modelName);
      requestBody.put("prompt", inputs.get(0)); // Ollama takes one input at a time

      ObjectMapper mapper = new ObjectMapper();
      byte[] jsonBody = mapper.writeValueAsBytes(requestBody);

      HttpRequest request = HttpRequest.builder()
          .method("POST")
          .uri(baseUrl + EMBEDDINGS_ENDPOINT)
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
