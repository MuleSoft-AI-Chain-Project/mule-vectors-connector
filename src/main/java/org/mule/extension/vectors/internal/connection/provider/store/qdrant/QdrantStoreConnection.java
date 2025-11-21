package org.mule.extension.vectors.internal.connection.provider.store.qdrant;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.validation.ConnectionValidationStrategies;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.QdrantOuterClass;

public class QdrantStoreConnection implements BaseStoreConnection {

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
    ConnectionValidationStrategies.validateQdrant(parameters);
    try {
      doHealthCheck();
    } catch (Exception e) {
      throw new ModuleException("Failed to connect to Qdrant store", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }

  void doHealthCheck() {
    try {
      // Assuming you have a method `healthCheckAsync` that returns ListenableFuture
      ListenableFuture<QdrantOuterClass.HealthCheckReply> healthCheckFuture = this.client.healthCheckAsync();
      // Make it synchronous by calling get()
      healthCheckFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new ModuleException("Failed during Qdrant health check", MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
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

    Collections.CreateCollection createCollection = Objects.requireNonNull(
                                                                           Collections.CreateCollection.newBuilder()
                                                                               .setCollectionName(storeName)
                                                                               .setVectorsConfig(vectorsConfig)
                                                                               .setStrictModeConfig(strictModeConfig)
                                                                               .build());

    this.client.createCollectionAsync(createCollection);
  }

  public void initialise() throws ConnectionException {
    String nonNullHost = Objects.requireNonNull(host);
    String nonNullApiKey = Objects.requireNonNull(apiKey);
    QdrantGrpcClient grpcClient = Objects.requireNonNull(
                                                         QdrantGrpcClient.newBuilder(nonNullHost, gprcPort, useTLS)
                                                             .withApiKey(nonNullApiKey)
                                                             .build());
    this.client = new QdrantClient(grpcClient);
  }
}
