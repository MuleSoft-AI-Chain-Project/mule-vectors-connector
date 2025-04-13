package org.mule.extension.vectors.internal.connection.store.pinecone;

import io.pinecone.clients.Pinecone;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;

public class PineconeStoreConnection implements BaseStoreConnection {

  private String cloud;
  private String region;
  private String apiKey;

  public PineconeStoreConnection(String cloud, String region, String apiKey) {
    this.cloud = cloud;
    this.region = region;
    this.apiKey = apiKey;
  }

  public String getCloud() {
    return cloud;
  }

  public String getRegion() {
    return region;
  }

  public String getApiKey() {
    return apiKey;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_PINECONE;
  }

  @Override
  public void connect() throws ConnectionException {

    try {

      Pinecone client = (new Pinecone.Builder(apiKey)).build();
      client.listIndexes();

    } catch (Exception e) {

      throw new ConnectionException("Impossible to connect to Pinecone.", e);
    }
  }

  @Override
  public void disconnect() {

    // Add disconnection logic if any.
  }

  @Override
  public boolean isValid() {

    try {

      Pinecone client = (new Pinecone.Builder(apiKey)).build();
      client.listIndexes();
      return true;

    } catch (Exception e) {

      return false;
    }
  }
}
