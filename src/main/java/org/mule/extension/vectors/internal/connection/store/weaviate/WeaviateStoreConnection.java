package org.mule.extension.vectors.internal.connection.store.weaviate;

import io.pinecone.clients.Pinecone;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;

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

  private static final String AUTH_CHECK_ENDPOINT = "/v1/schema";

  public WeaviateStoreConnection(String scheme, String host, Integer port,
                                 boolean securedGrpc, Integer grpcPort, boolean useGrpcForInserts,
                                 String apiKey, boolean avoidDups, String consistencyLevel) {
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.securedGrpc = securedGrpc;
    this.grpcPort = grpcPort;
    this.useGrpcForInserts = useGrpcForInserts;
    this.apikey = apiKey;
    this.avoidDups = avoidDups;
    this.consistencyLevel = consistencyLevel;
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
  public void connect() throws ConnectionException {
    try {
      testConnection();
    } catch (Exception e) {
      throw new ConnectionException("Impossible to connect to Weaviate.", e);
    }
  }

  @Override
  public void disconnect() {
    // Add disconnection logic if any.
  }

  @Override
  public boolean isValid() {
    try {
      testConnection();
      return true;
    } catch (Exception e) {
      return false;
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
