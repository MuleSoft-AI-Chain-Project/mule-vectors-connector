package org.mule.extension.vectors.internal.connection.store.chroma;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;

public class ChromaStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChromaStoreConnection.class);

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

  /**
   * Changed from isValid() to validate() for MuleSoft Connector compliance.
   * Now checks for required parameters.
   */
  @Override
  public void validate() {
    try {
      if (url == null) {
        throw new IllegalArgumentException("URL is required for Chroma connection.");
      }
      doHttpRequest();
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to Chroma", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  private void doHttpRequest() throws ConnectionException {

    try {

        HttpRequestBuilder requestBuilder = HttpRequest.builder()
          .method("GET")
          .uri(url + API_ENDPOINT)
          .addHeader("Content-Type", "application/json")
          .addHeader("Accept", "application/json");

        HttpRequestOptions options = HttpRequestOptions.builder()
            .responseTimeout(30000)
            .followsRedirect(false)
            .build();

        HttpResponse connectionResponse = httpClient.send(requestBuilder.build(), options);

        if (connectionResponse.getStatusCode() != 200) {
          String errorBody = new String(connectionResponse.getEntity().getBytes());
          String errorMsg = String.format("Unable to connect to Chroma. Status: %d - %s", 
              connectionResponse.getStatusCode(), errorBody);
          LOGGER.error(errorMsg);
          throw new ConnectionException(errorMsg);
        }
    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      LOGGER.error("Impossible to connect to Chroma", e);
      throw new ConnectionException("Impossible to connect to Chroma", e);
    }
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }
}
