package org.mule.extension.vectors.internal.connection.store.weaviate;

import io.pinecone.clients.Pinecone;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;

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

  private static final String AUTH_CHECK_ENDPOINT = "/v1/schema";

  public WeaviateStoreConnection(WeaviateStoreConnectionParameters parameters) {
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
  }

  private void testConnection() throws Exception {
    String urlString;
    if (port != null && port != 0) {
      urlString = String.format("%s://%s:%s%s", scheme, host, port, AUTH_CHECK_ENDPOINT);
    } else {
      urlString = String.format("%s://%s%s", scheme, host, AUTH_CHECK_ENDPOINT);
    }
    URL url = new URL(urlString);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setConnectTimeout(5000);
    conn.setReadTimeout(5000);
    if (apikey != null && !apikey.isEmpty()) {
      conn.setRequestProperty("Authorization", "Bearer " + apikey);
    }
    int responseCode = conn.getResponseCode();
    if (responseCode != 200) {
      InputStream errorStream = conn.getErrorStream();
      String errorBody = "";
      if (errorStream != null) {
        byte[] bytes = errorStream.readAllBytes();
        errorBody = new String(bytes, StandardCharsets.UTF_8);
      }
      conn.disconnect();
      String msg = String.format("Weaviate auth check failed with status code: %d. Response: %s", responseCode, errorBody);
      throw new Exception(msg);
    }
    conn.disconnect();
  }
}
