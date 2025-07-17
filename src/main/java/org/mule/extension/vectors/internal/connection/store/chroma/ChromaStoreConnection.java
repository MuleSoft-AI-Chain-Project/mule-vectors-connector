package org.mule.extension.vectors.internal.connection.store.chroma;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;

public class ChromaStoreConnection implements BaseStoreConnection {
  private static final String API_ENDPOINT = "/api/v1";

  private String url;
  private final ChromaStoreConnectionParameters parameters;
  private HttpClient httpClient;

  public ChromaStoreConnection(ChromaStoreConnectionParameters parameters, HttpClient httpClient) {
    this.parameters = parameters;
    this.url = parameters.getUrl();
    this.httpClient = httpClient;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_CHROMA;
  }

  @Override
  public void disconnect() {

  }

  @Override
  public BaseStoreConnectionParameters getConnectionParameters() {
    return parameters;
  }

  private Map<String, String> buildHeaders(String contentType) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", contentType);
    headers.put("Accept", contentType);
    return headers;
  }

  /**
   * Changed from isValid() to validate() for MuleSoft Connector compliance.
   * Now checks for required parameters.
   */
  @Override
  public void validate() {
    try {
      if (url == null) {
        throw new ModuleException("URL is required for Chroma connection.", MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
      }
      doHttpRequest().get();
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to Chroma", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  private CompletableFuture<Void> doHttpRequest() throws ConnectionException {
    return HttpRequestHelper.executeGetRequest(httpClient, url + API_ENDPOINT, buildHeaders("application/json"), 30000)
        .thenAccept(connectionResponse -> {
          if (connectionResponse.getStatusCode() != 200) {
            try {
              String errorBody = new String(connectionResponse.getEntity().getBytes());
              String errorMsg = String.format("Unable to connect to Chroma. Status: %d - %s", connectionResponse.getStatusCode(), errorBody);
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
