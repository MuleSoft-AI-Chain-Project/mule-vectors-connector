package org.mule.extension.vectors.internal.connection.store.qdrant;

import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.QdrantOuterClass;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mule.extension.vectors.internal.connection.store.qdrant.QdrantStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;

public class QdrantStoreConnection implements BaseStoreConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(QdrantStoreConnection.class);

  private final String host;
  private final int gprcPort;
  private final boolean useTLS;
  private final String textSegmentKey;
  private final String apiKey;

  private QdrantClient client;
  private final QdrantStoreConnectionParameters parameters;

  public QdrantStoreConnection(QdrantStoreConnectionParameters parameters) {
    this.parameters = parameters;
    this.host = parameters.getHost();
    this.gprcPort = parameters.getGprcPort();
    this.useTLS = parameters.isUseTLS();
    this.textSegmentKey = parameters.getTextSegmentKey();
    this.apiKey = parameters.getApiKey();
  }

  public String getHost() {
    return host;
  }

  public int getGprcPort() {
    return gprcPort;
  }

  public boolean isUseTLS() {
    return useTLS;
  }

  public String getTextSegmentKey() {
    return textSegmentKey;
  }

  public String getApiKey() {
    return apiKey;
  }

  public QdrantClient getClient() {
    return client;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_QDRANT;
  }

  @Override
  public void disconnect() {

    this.client.close();
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
    if (parameters.getHost() == null || parameters.getHost().isBlank()) {
      throw new ModuleException("Host is required for Qdrant connection", MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
    }
    if (parameters.getGprcPort() <= 0) {
      throw new ModuleException("gprcPort is required for Qdrant connection and must be > 0", MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
    }
    if (parameters.getTextSegmentKey() == null || parameters.getTextSegmentKey().isBlank()) {
      throw new ModuleException("TextSegmentKey is required for Qdrant connection", MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
    }
    if (parameters.getApiKey() == null || parameters.getApiKey().isBlank()) {
      throw new ModuleException("API Key is required for Qdrant connection", MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
    }
    try {
      doHealthCheck();
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to Qdrant store", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  private void doHealthCheck() throws Exception {

    // Assuming you have a method `healthCheckAsync` that returns ListenableFuture
    ListenableFuture<QdrantOuterClass.HealthCheckReply> healthCheckFuture = this.client.healthCheckAsync();

    // Make it synchronous by calling get()
    healthCheckFuture.get();
  }

  public void createCollection(String storeName, int dimension) {

    Collections.StrictModeConfig strictModeConfig = Collections.StrictModeConfig.newBuilder()
        .setEnabled(false)
        .build();

    Collections.VectorsConfig vectorsConfig = Collections.VectorsConfig.newBuilder()
        .setParams(Collections.VectorParams.newBuilder()
                       .setSize(dimension)
                       .setDistance(Collections.Distance.Cosine)
                       .build())
        .build();

    this.client.createCollectionAsync(
        Collections.CreateCollection.newBuilder()
            .setCollectionName(storeName)
            .setVectorsConfig(vectorsConfig)
            .setStrictModeConfig(strictModeConfig)
            .build());
  }
  public void initialise() throws ConnectionException {
      this.client = new QdrantClient(
          QdrantGrpcClient.newBuilder(host, gprcPort, useTLS)
              .withApiKey(apiKey)
              .build()
      );
  }
}
