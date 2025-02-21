package org.mule.extension.vectors.internal.connection.store.milvus;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("milvus")
@DisplayName("Milvus")
public class MilvusStoreConnectionProvider extends BaseStoreConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(MilvusStoreConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private MilvusStoreConnectionParameters milvusStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      MilvusStoreConnection milvusStoreConnection = new MilvusStoreConnection(milvusStoreConnectionParameters.getUrl(), milvusStoreConnectionParameters.getToken());
      milvusStoreConnection.connect();
      return milvusStoreConnection;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Milvus.", e);
    }
  }

  @Override
  public void disconnect(BaseStoreConnection connection) {

    connection.disconnect();
  }

  @Override
  public ConnectionValidationResult validate(BaseStoreConnection connection) {

    try {
      if (connection.isValid()) {
        return ConnectionValidationResult.success();
      } else {
        return ConnectionValidationResult.failure("Failed to validate connection to Milvus", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to Milvus", e);
    }
  }
}
