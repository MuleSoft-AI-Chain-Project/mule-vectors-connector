package org.mule.extension.vectors.internal.connection.store.milvus;

import dev.langchain4j.internal.Utils;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.extension.vectors.internal.constant.Constants;

public class MilvusStoreConnection implements BaseStoreConnection {

  private String uri;
  private String host;
  private Integer port;
  private String token;
  private String username;
  private String password;
  private String databaseName;
  private String indexType;
  private String metricType;
  private String consistencyLevel;
  private boolean autoFlushOnInsert;
  private String idFieldName;
  private String textFieldName;
  private String metadataFieldName;
  private String vectorFieldName;
  private MilvusServiceClient client;

  private final MilvusStoreConnectionParameters milvusStoreConnectionParameters;

  public MilvusStoreConnection(final MilvusStoreConnectionParameters milvusStoreConnectionParameters) {
    this.milvusStoreConnectionParameters = milvusStoreConnectionParameters;
    this.host = milvusStoreConnectionParameters.getHost();
    this.port = milvusStoreConnectionParameters.getPort();
    this.token = milvusStoreConnectionParameters.getToken();
    this.username = milvusStoreConnectionParameters.getUsername();
    this.password = milvusStoreConnectionParameters.getPassword();
    this.databaseName = milvusStoreConnectionParameters.getDatabaseName();
    this.indexType = milvusStoreConnectionParameters.getIndexType();
    this.metricType = milvusStoreConnectionParameters.getMetricType();
    this.consistencyLevel = milvusStoreConnectionParameters.getConsistencyLevel();
    this.autoFlushOnInsert = milvusStoreConnectionParameters.isAutoFlushOnInsert();
    this.idFieldName = milvusStoreConnectionParameters.getIdFieldName();
    this.textFieldName = milvusStoreConnectionParameters.getTextFieldName();
    this.metadataFieldName = milvusStoreConnectionParameters.getMetadataFieldName();
    this.vectorFieldName = milvusStoreConnectionParameters.getVectorFieldName();
  }

  public String getIndexType() {
    return indexType;
  }

  public String getMetricType() {
    return metricType;
  }

  public String getConsistencyLevel() {
    return consistencyLevel;
  }

  public boolean isAutoFlushOnInsert() {
    return autoFlushOnInsert;
  }

  public String getIdFieldName() {
    return idFieldName;
  }

  public String getTextFieldName() {
    return textFieldName;
  }

  public String getMetadataFieldName() {
    return metadataFieldName;
  }

  public String getVectorFieldName() {
    return vectorFieldName;
  }

  public MilvusServiceClient getClient() {
    return client;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_MILVUS;
  }

  @Override
  public BaseStoreConnectionParameters getConnectionParameters() {
    return milvusStoreConnectionParameters;
  }


  public void connect() {

    ConnectParam.Builder connectBuilder = ConnectParam.newBuilder();

    connectBuilder
        .withToken(token)
        .withAuthorization((String)Utils.getOrDefault(username, ""), (String)Utils.getOrDefault(password, ""));

    if (uri != null && !uri.isBlank()) connectBuilder.withUri(uri);
    if (host != null && !host.isBlank()) connectBuilder.withHost(host);
    if (port != null && port != 0) connectBuilder.withPort((Integer)Utils.getOrDefault(port, 19530));
    if (databaseName != null && !databaseName.isBlank()) connectBuilder.withDatabaseName(databaseName);


    client = new MilvusServiceClient(connectBuilder.build());
  }

  @Override
  public void disconnect() {

    if(client != null) {

      client.close();
    }
  }

  @Override
  public void validate() {

  }


  public boolean isValid() {
    return client.checkHealth().getStatus() == 0;
  }
}
