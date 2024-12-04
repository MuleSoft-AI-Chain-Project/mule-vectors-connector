package org.mule.extension.vectors.internal.connection.store.pgvector;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("pgVector")
@DisplayName("PGVector")
public class PGVectorStoreConnectionProvider  extends BaseStoreConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(PGVectorStoreConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private PGVectorStoreConnectionParameters pgVectorStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    throw new ConnectionException("Failed to connect to PGVector. Test Connection not supported yet.", null);
  }

  @Override
  public void disconnect(BaseStoreConnection connection) {

  }

  @Override
  public ConnectionValidationResult validate(BaseStoreConnection connection) {

    return ConnectionValidationResult.failure("Failed to validate connection to PGVector", null);
  }

}