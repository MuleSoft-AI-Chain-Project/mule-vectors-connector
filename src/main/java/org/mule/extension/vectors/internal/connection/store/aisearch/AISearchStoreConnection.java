package org.mule.extension.vectors.internal.connection.store.aisearch;

import com.azure.search.documents.SearchServiceVersion;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;

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

  }

  @Override
  public BaseStoreConnectionParameters getConnectionParameters() {
    return parameters;
  }

  /**
   * Changed from isValid() to validate() for MuleSoft Connector compliance.
   * Now checks for required parameters.
   */
  @Override
  public void validate() {
    if (url == null ) {
      throw new IllegalArgumentException("URL is required for AI Search connection.");
    }
    if (apiKey == null) {
      throw new IllegalArgumentException("API Key is required for AI Search connection.");
    }
    try {
      doAuthenticatedHttpRequest();
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to AI search", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  private void doAuthenticatedHttpRequest() throws ConnectionException {

    try {

      // Construct the endpoint URL
      String endpoint = url + "?api-version=" + 
          SearchServiceVersion.getLatest().toString().substring(1).replace("_", "-");

      HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .method("GET")
          .uri(endpoint)
          .addHeader("Content-Type", "application/json")
          .addHeader("api-key", apiKey);

      HttpRequestOptions options = HttpRequestOptions.builder()
          .responseTimeout(30000)
          .followsRedirect(false)
          .build();

      HttpResponse connectionResponse = httpClient.send(requestBuilder.build(), options);

      if (connectionResponse.getStatusCode() != 200) {
        String errorBody = new String(connectionResponse.getEntity().getBytes());
        String errorMsg = String.format("Unable to connect to AI search. Status: %d - %s", 
            connectionResponse.getStatusCode(), errorBody);
        LOGGER.error(errorMsg);
        throw new ConnectionException(errorMsg);
      }

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      LOGGER.error("Impossible to connect to AI Search", e);
      throw new ConnectionException("Impossible to connect to AI Search", e);
    }
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }
}
