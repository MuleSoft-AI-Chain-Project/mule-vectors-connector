package org.mule.extension.vectors.internal.connection.store.pinecone;

import io.pinecone.clients.Pinecone;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnectionParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;

public class PineconeStoreConnection implements BaseStoreConnection {

  private String cloud;
  private String region;
  private String apiKey;
  private final PineconeStoreConnectionParameters parameters;
  private Pinecone client;

  public PineconeStoreConnection(PineconeStoreConnectionParameters parameters) {
    this.parameters = parameters;
    this.cloud = parameters.getCloud();
    this.region = parameters.getRegion();
    this.apiKey = parameters.getApiKey();
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

  public Pinecone getClient() {
    return client;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_PINECONE;
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
    if (parameters.getCloud() == null || parameters.getCloud().isBlank()) {
      throw new IllegalArgumentException("Cloud is required for Pinecone connection");
    }
    if (parameters.getRegion() == null || parameters.getRegion().isBlank()) {
      throw new IllegalArgumentException("Region is required for Pinecone connection");
    }
    if (parameters.getApiKey() == null || parameters.getApiKey().isBlank()) {
      throw new IllegalArgumentException("API Key is required for Pinecone connection");
    }
    try {
      client.listIndexes();
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to Pinecone store", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  public void initialise() {
    client = (new Pinecone.Builder(apiKey)).build();
  }
}
