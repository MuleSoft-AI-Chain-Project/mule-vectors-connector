package org.mule.extension.vectors.internal.connection.store.milvus;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.connection.ConnectionException;

public class MilvusStoreConnection implements BaseStoreConnection {

  private String url;
  private String token;
  private MilvusServiceClient client;

  public MilvusStoreConnection(String url, String token) {
    this.url = url;
    this.token = token;
  }

  public String getUrl() {
    return url;
  }

  public String getToken() { return token; }

  public MilvusServiceClient getClient() {
    return client;
  }

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_MILVUS;
  }

  @Override
  public void connect() {

    ConnectParam connectParam = ConnectParam.newBuilder()
        .withUri(url)
        .withToken(token)
        .build();
    client = new MilvusServiceClient(connectParam);
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
