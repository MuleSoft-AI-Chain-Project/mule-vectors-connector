package org.mule.extension.vectors.internal.store.milvus;

import org.mule.extension.vectors.internal.model.BaseModelConnection;
import org.mule.extension.vectors.internal.model.BaseModelConnectionProvider;
import org.mule.extension.vectors.internal.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.store.BaseStoreConnectionProvider;
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

    throw new ConnectionException("Failed to connect to store", null);
  }

  @Override
  public void disconnect(BaseStoreConnection connection) {

  }

  @Override
  public ConnectionValidationResult validate(BaseStoreConnection connection) {

    return ConnectionValidationResult.failure("Failed to validate connection to store", null);
  }
}
