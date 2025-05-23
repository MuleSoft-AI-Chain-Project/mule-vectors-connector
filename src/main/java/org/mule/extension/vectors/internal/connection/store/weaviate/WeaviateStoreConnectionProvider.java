package org.mule.extension.vectors.internal.connection.store.weaviate;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnectionParameters;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("weaviate")
@DisplayName("Weaviate")
public class WeaviateStoreConnectionProvider extends BaseStoreConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeaviateStoreConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private WeaviateStoreConnectionParameters weaviateStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      WeaviateStoreConnection weaviateStoreConnection =
          new WeaviateStoreConnection(weaviateStoreConnectionParameters.getScheme(),
                                      weaviateStoreConnectionParameters.getHost(),
                                      weaviateStoreConnectionParameters.getPort(),
                                      weaviateStoreConnectionParameters.getApiKey(),
                                      weaviateStoreConnectionParameters.isAvoidDups(),
                                      weaviateStoreConnectionParameters.getConsistencyLevel());
      weaviateStoreConnection.connect();
      return weaviateStoreConnection;

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Pinecone", e);
    }
  }

  @Override
  public void disconnect(BaseStoreConnection connection) {

    try {

      connection.disconnect();
    } catch (Exception e) {

      LOGGER.error("Failed to close connection", e);
    }
  }

  @Override
  public ConnectionValidationResult validate(BaseStoreConnection connection) {

    try {

      if (connection.isValid()) {
        return ConnectionValidationResult.success();
      } else {
        return ConnectionValidationResult.failure("Failed to validate connection to Weaviate", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to Weaviate", e);
    }
  }

}
