package org.mule.extension.vectors.internal.connection.model.azureopenai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.extension.vectors.internal.connection.model.BaseTextModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AzureOpenAIModelConnection implements BaseTextModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureOpenAIModelConnection.class);

  private String endpoint;
  private String apiKey;
  private final String apiVersion;
  private final long timeout;
  private final HttpClient httpClient;

  public AzureOpenAIModelConnection(String endpoint, String apiKey, String apiVersion, long timeout, HttpClient httpClient) {
    this.endpoint = endpoint;
    this.apiKey = apiKey;
    this.apiVersion = apiVersion;
    this.timeout = timeout;
    this.httpClient = httpClient;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public String getApiKey() {
    return apiKey;
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_AZURE_OPENAI;
  }


  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public void validate() {
    try {
      testCredentials();
    } catch (Exception e) {
      LOGGER.error("Failed to validate connection to Azure Open AI", e);
      throw new ModuleException("Failed to validate connection to  Azure Open AI", MuleVectorsErrorType.INVALID_CONNECTION, e);

    }
  }

  public void testCredentials() {
    try {
      String url = String.format("%s/openai/deployments/test-connection/embeddings?api-version=%s", endpoint, apiVersion);
      
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("input", List.of("")); // Minimal payload with empty string
      
      ObjectMapper mapper = new ObjectMapper();
      byte[] jsonBody = mapper.writeValueAsBytes(requestBody);

      HttpRequest request = HttpRequest.builder()
          .method("POST")
          .uri(url)
          .addHeader("api-key", apiKey)
          .addHeader("Content-Type", "application/json")
          .entity(new ByteArrayHttpEntity(jsonBody))
          .build();

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(request, options);

      // 401/403 indicate auth failures
      if (response.getStatusCode() == 401 || response.getStatusCode() == 403) {
        LOGGER.error("Authentication failed. Please check your credentials.");
        throw new RuntimeException("Invalid credentials");
      }
      
      // Either 404 with DeploymentNotFound or 400 with Bad Request is expected since we're using a fake deployment
      // Any other error indicates a problem
      if (response.getStatusCode() != 404 && response.getStatusCode() != 400) {
        String errorMsg = String.format("Unexpected response code: %d", response.getStatusCode());
        LOGGER.error(errorMsg);
        throw new RuntimeException(errorMsg);
      }

    } catch (RuntimeException re) {

      throw re;

    } catch (Exception e) {
      LOGGER.error("Failed to validate credentials", e);
      throw new RuntimeException("Failed to validate credentials", e);
    }
  }

  @Override
  public Object generateTextEmbeddings(List<String> inputs, String deploymentName) {

    if(inputs == null || inputs.isEmpty()) {
      throw new IllegalArgumentException("Input list cannot be null or empty");
    }
    if(deploymentName == null || deploymentName.isEmpty()) {
      throw new IllegalArgumentException("Model name cannot be null or empty");
    }
    LOGGER.debug("Generating embeddings for {} inputs using model: {}", inputs.size(), deploymentName);

    try {
      String url = String.format("%s/openai/deployments/%s/embeddings?api-version=%s", endpoint, deploymentName, apiVersion);
      
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("input", inputs);
      
      ObjectMapper mapper = new ObjectMapper();
      byte[] jsonBody = mapper.writeValueAsBytes(requestBody);

      HttpRequest request = HttpRequest.builder()
          .method("POST")
          .uri(url)
          .addHeader("api-key", apiKey)
          .addHeader("Content-Type", "application/json")
          .entity(new ByteArrayHttpEntity(jsonBody))
          .build();

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout((int)timeout)
          .followsRedirect(false)
          .build();

      HttpResponse response = httpClient.send(request, options);

      validateResponse(response);

      return new String(response.getEntity().getBytes());

    } catch (Exception e) {
      LOGGER.error("Error generating embeddings", e);
      throw new RuntimeException("Failed to generate embeddings", e);
    }
  }

  private void validateResponse(HttpResponse response) {

    if (response.getStatusCode() != 200) {
      
      String responseBody = "";

      try {
        
        responseBody = new String(response.getEntity().getBytes());
        LOGGER.error("Error (HTTP {}): {}", response.getStatusCode(), responseBody);

      } catch (IOException e) {
        
        LOGGER.error("Error reading response body", e);
      }
      
      MuleVectorsErrorType muleVectorsErrorType = response.getStatusCode() == 429 ?
          MuleVectorsErrorType.AI_SERVICES_RATE_LIMITING_ERROR : MuleVectorsErrorType.AI_SERVICES_FAILURE;

      throw new ModuleException(
          String.format("Error while generating embeddings with \"AZURE OPEN AI\" embedding model service. Response code: %s. Response %s.",
              response.getStatusCode(), responseBody),
          muleVectorsErrorType);
    }
  }
}
