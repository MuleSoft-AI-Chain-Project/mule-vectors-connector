package org.mule.extension.vectors.internal.connection.store.milvus;

import dev.langchain4j.internal.Utils;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;

public class MilvusStoreConnection implements BaseStoreConnection {

  private String host;
  private Integer port;
  private String token;
  private String username;
  private String password;
  private String databaseName;
  private String idFieldName;
  private String textFieldName;
  private String metadataFieldName;
  private String vectorFieldName;
  private MilvusServiceClient client;

  public MilvusStoreConnection(String url, Integer port, String token, String username, String password, String databaseName,
                               String idFieldName, String textFieldName, String metadataFieldName, String vectorFieldName) {
    this.host = url;
    this.port = port;
    this.token = token;
    this.username = username;
    this.password = password;
    this.databaseName = databaseName;
    this.idFieldName = idFieldName;
    this.textFieldName = textFieldName;
    this.metadataFieldName = metadataFieldName;
    this.vectorFieldName = vectorFieldName;
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
  public void connect() {

    ConnectParam.Builder connectBuilder = ConnectParam.newBuilder()
        .withHost((String) Utils.getOrDefault(host, "localhost"))
        .withPort((Integer)Utils.getOrDefault(port, 19530))
        .withToken(token)
        .withAuthorization((String)Utils.getOrDefault(username, ""), (String)Utils.getOrDefault(password, ""));

    if (databaseName != null) {
      connectBuilder.withDatabaseName(databaseName);
    }

    client = new MilvusServiceClient(connectBuilder.build());
  }

  @Override
  public void disconnect() {

    if(client != null) {

      client.close();
    }
  }

  @Override
  public boolean isValid() {
    return client.checkHealth().getStatus() == 0;
  }
}
