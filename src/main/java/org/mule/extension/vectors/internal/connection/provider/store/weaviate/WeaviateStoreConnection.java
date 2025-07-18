package org.mule.extension.vectors.internal.connection.provider.store.weaviate;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeaviateStoreConnection implements BaseStoreConnection {

  private String scheme;
  private String host;
  private Integer port;
  private boolean securedGrpc;
  private Integer grpcPort;
  private boolean useGrpcForInserts;
  private String apikey;
  private boolean avoidDups;
  private String consistencyLevel;
  private final WeaviateStoreConnectionParameters parameters;
  private HttpClient httpClient;
  private static final String AUTH_CHECK_ENDPOINT = "/v1/schema";

  private static final Logger LOGGER = LoggerFactory.getLogger(WeaviateStoreConnectionProvider.class);

  public WeaviateStoreConnection(WeaviateStoreConnectionParameters parameters, final HttpClient httpClient) {
    this.parameters = parameters;
    this.scheme = parameters.getScheme();
    this.host = parameters.getHost();
    this.port = parameters.getPort();
    this.securedGrpc = parameters.isSecuredGrpc();
    this.grpcPort = parameters.getGrpcPort();
    this.useGrpcForInserts = parameters.isUseGrpcForInserts();
    this.apikey = parameters.getApiKey();
    this.avoidDups = parameters.isAvoidDups();
    this.consistencyLevel = parameters.getConsistencyLevel();
    this.httpClient = httpClient;
  }

  public HttpClient getHttpClient() {
    return httpClient;
  }
  public String getScheme() {
    return scheme;
  }

  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public boolean isSecuredGrpc() { return securedGrpc; }

  public Integer getGrpcPort() { return grpcPort; }

  public boolean isUseGrpcForInserts() { return useGrpcForInserts; }

  public String getApikey() {
    return apikey;
  }

  public boolean isAvoidDups() {
    return avoidDups;
  }

  public String getConsistencyLevel() {
    return consistencyLevel;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_WEAVIATE;
  }

  @Override
  public void disconnect() {
    // Add disconnection logic if any.
  }

  @Override
  public BaseStoreConnectionParameters getConnectionParameters() {
    return parameters;
  }

  private Map<String, String> buildHeaders(String contentType) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", "Bearer " + apikey);
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
    if (parameters.getScheme() == null || parameters.getScheme().isBlank()) {
      throw new ModuleException("Scheme is required for Weaviate connection", MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
    }
    if (parameters.getHost() == null || parameters.getHost().isBlank()) {
      throw new ModuleException("Host is required for Weaviate connection", MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
    }
    if (parameters.getApiKey() == null || parameters.getApiKey().isBlank()) {
      throw new ModuleException("API Key is required for Weaviate connection", MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
    }
    try {
      testConnection().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ModuleException("Failed to connect to Weaviate store", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to Weaviate store", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  private CompletableFuture<Void> testConnection() {
    try {
      String urlString;
      if (port != null && port != 0) {
        urlString = String.format("%s://%s:%s%s", scheme, host, port, AUTH_CHECK_ENDPOINT);
      } else {
        urlString = String.format("%s://%s%s", scheme, host, AUTH_CHECK_ENDPOINT);
      }

      return HttpRequestHelper.executeGetRequest(httpClient, urlString, buildHeaders("application/json"), 30000)
          .thenAccept(connectionResponse -> {
            if (connectionResponse.getStatusCode() != 200) {
              try {
                String errorBody = new String(connectionResponse.getEntity().getBytes());
                String errorMsg = String.format("Unable to connect to Weaviate. Status: %d - %s", connectionResponse.getStatusCode(), errorBody);
                LOGGER.error(errorMsg);
                throw new ModuleException(errorMsg, MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
              } catch (IOException e) {
                throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
               }
            }
          });
    } catch (Exception e) {
      throw new ModuleException("Failed during Weaviate test connection", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }
}
