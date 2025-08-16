package org.mule.extension.vectors.internal.connection.provider.store.aisearch;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.helper.validation.ConnectionValidationStrategies;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.azure.search.documents.SearchServiceVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AISearchStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(AISearchStoreConnection.class);

  private String url;
  private String apiKey;
  private final AISearchStoreConnectionParameters parameters;
  private HttpClient httpClient;

  public AISearchStoreConnection(AISearchStoreConnectionParameters parameters, HttpClient httpClient) {
    this.parameters = parameters;
    this.url = parameters.getUrl();
    this.apiKey = parameters.getApiKey();
    this.httpClient = httpClient;
  }

  public String getUrl() {
    return url;
  }

  public String getApiKey() {
    return apiKey;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_AI_SEARCH;
  }

  @Override
  public void disconnect() {
    // HttpClient lifecycle is managed by the provider
  }

  @Override
  public BaseStoreConnectionParameters getConnectionParameters() {
    return parameters;
  }

  private Map<String, String> buildHeaders(String contentType) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", contentType);
    headers.put("api-key", apiKey);
    return headers;
  }

  /**
   * Changed from isValid() to validate() for MuleSoft Connector compliance.
   * Now checks for required parameters.
   */
  @Override
  public void validate() {
    ConnectionValidationStrategies.validateAISearch(parameters);
    try {
      doAuthenticatedHttpRequest().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ModuleException("Failed to connect to AI search", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to AI search", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  private CompletableFuture<Void> doAuthenticatedHttpRequest() throws ConnectionException {
    // Construct the endpoint URL
    String endpoint = url + "?api-version=" +
        SearchServiceVersion.getLatest().toString().substring(1).replace("_", "-");

    return HttpRequestHelper.executeGetRequest(httpClient, endpoint, buildHeaders("application/json"), 30000)
        .thenAccept(connectionResponse -> {
          if (connectionResponse.getStatusCode() != 200) {
            try {
              String errorBody = new String(connectionResponse.getEntity().getBytes());
              String errorMsg =
                  String.format("Unable to connect to AI search. Status: %d - %s", connectionResponse.getStatusCode(), errorBody);
              LOGGER.error(errorMsg);
              throw new ModuleException(errorMsg, MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
            } catch (IOException e) {
              throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
            }
          }
        });
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }
}
