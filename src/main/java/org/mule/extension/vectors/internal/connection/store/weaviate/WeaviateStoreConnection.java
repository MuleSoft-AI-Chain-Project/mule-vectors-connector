package org.mule.extension.vectors.internal.connection.store.weaviate;

import io.pinecone.clients.Pinecone;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.client.HttpRequestOptions;
import org.mule.runtime.http.api.domain.message.request.HttpRequest;
import org.mule.runtime.http.api.domain.message.request.HttpRequestBuilder;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

  /**
   * Changed from isValid() to validate() for MuleSoft Connector compliance.
   * Now checks for required parameters.
   */
  @Override
  public void validate() {
    if (parameters.getScheme() == null || parameters.getScheme().isBlank()) {
      throw new IllegalArgumentException("Scheme is required for Weaviate connection");
    }
    if (parameters.getHost() == null || parameters.getHost().isBlank()) {
      throw new IllegalArgumentException("Host is required for Weaviate connection");
    }
    if (parameters.getApiKey() == null || parameters.getApiKey().isBlank()) {
      throw new IllegalArgumentException("API Key is required for Weaviate connection");
    }
    try {
      testConnection();
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to Weaviate store", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  private void testConnection() throws Exception {
    String urlString;
    if (port != null && port != 0) {
      urlString = String.format("%s://%s:%s%s", scheme, host, port, AUTH_CHECK_ENDPOINT);
    } else {
      urlString = String.format("%s://%s%s", scheme, host, AUTH_CHECK_ENDPOINT);
    }

    HttpRequestBuilder requestBuilder = HttpRequest.builder()
      .method("GET")
      .uri(urlString)
      .addHeader("Authorization", "Bearer " + apikey)
      .addHeader("Content-Type", "application/json")
      .addHeader("Accept", "application/json");

    HttpRequestOptions options = HttpRequestOptions.builder()
        .responseTimeout(30000)
        .followsRedirect(false)
        .build();

    HttpResponse connectionResponse = httpClient.send(requestBuilder.build(), options);

    if (connectionResponse.getStatusCode() != 200) {
      String errorBody = new String(connectionResponse.getEntity().getBytes());
      String errorMsg = String.format("Unable to connect to Weaviate. Status: %d - %s", 
          connectionResponse.getStatusCode(), errorBody);
      throw new ConnectionException(errorMsg);
    }
  }
}
