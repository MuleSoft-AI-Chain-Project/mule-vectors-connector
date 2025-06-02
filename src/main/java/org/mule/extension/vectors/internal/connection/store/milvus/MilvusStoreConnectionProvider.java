package org.mule.extension.vectors.internal.connection.store.milvus;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("milvus")
@DisplayName("Milvus")
@ExternalLib(name = "LangChain4J Milvus",
    type=DEPENDENCY,
    description = "LangChain4J Milvus",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-milvus:1.0.1-beta6")
public class MilvusStoreConnectionProvider extends BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private MilvusStoreConnectionParameters milvusStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      MilvusStoreConnection milvusStoreConnection = new MilvusStoreConnection(
          milvusStoreConnectionParameters.getHost(),
          milvusStoreConnectionParameters.getPort(),
          milvusStoreConnectionParameters.getToken(),
          milvusStoreConnectionParameters.getUsername(),
          milvusStoreConnectionParameters.getPassword(),
          milvusStoreConnectionParameters.getDatabaseName());
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
