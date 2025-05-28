package org.mule.extension.vectors.internal.connection.store.pgvector;

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

@Alias("pgVector")
@DisplayName("PGVector")
@ExternalLib(name = "LangChain4J PGVector",
    type=DEPENDENCY,
    description = "LangChain4J PGVector",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-pgvector:1.0.1-beta6")
public class PGVectorStoreConnectionProvider  extends BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private PGVectorStoreConnectionParameters pgVectorStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      PGVectorStoreConnection pgVectorStoreConnection =
          new PGVectorStoreConnection(pgVectorStoreConnectionParameters.getHost(),
                                      pgVectorStoreConnectionParameters.getPort(),
                                      pgVectorStoreConnectionParameters.getDatabase(),
                                      pgVectorStoreConnectionParameters.getUser(),
                                      pgVectorStoreConnectionParameters.getPassword());
      pgVectorStoreConnection.connect();
      return pgVectorStoreConnection;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to PGVector.", e);
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
        return ConnectionValidationResult.failure("Failed to validate connection to PGVector", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to PGVector", e);
    }
  }
}
