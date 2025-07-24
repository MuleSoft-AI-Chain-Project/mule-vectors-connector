package org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("huggingFace")
@DisplayName("Hugging Face")
public class HuggingFaceModelConnection implements BaseModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(HuggingFaceModelConnection.class);

  private static final String AUTH_ENDPOINT = "https://huggingface.co/api/whoami-v2";

  private final String apiKey;
  private final HttpClient httpClient;
  private final long timeout;

  public HuggingFaceModelConnection(String apiKey, long timeout, HttpClient httpClient) {
    this.apiKey = apiKey;
    this.timeout = timeout;
    this.httpClient = httpClient;
  }

  public String getApiKey() {
    return this.apiKey;
  }

  public HttpClient getHttpClient() {
    return this.httpClient;
  }

  public long getTimeout() {
    return this.timeout;
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
      throw new ModuleException("Failed to validate connection to Hugging Face", MuleVectorsErrorType.INVALID_CONNECTION,
                                e.getCause());
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

  private Map<String, String> buildAuthHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + apiKey);
    return headers;
  }

  private String handleErrorResponse(HttpResponse response, String message) {
    try {
      String errorBody = new String(response.getEntity().getBytes());
      String errorMsg = String.format("%s. Status: %d - %s", message, response.getStatusCode(), errorBody);
      throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
    } catch (IOException e) {
      throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
    }
  }
}
