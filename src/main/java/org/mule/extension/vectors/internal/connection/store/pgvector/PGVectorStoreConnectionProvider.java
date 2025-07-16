package org.mule.extension.vectors.internal.connection.store.pgvector;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import java.sql.SQLException;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("pgVector")
@DisplayName("PGVector")
@ExternalLib(name = "LangChain4J PGVector",
    type=DEPENDENCY,
    description = "LangChain4J PGVector",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-pgvector:1.1.0-beta7")
public class PGVectorStoreConnectionProvider implements BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private PGVectorStoreConnectionParameters pgVectorStoreConnectionParameters;
  private PGVectorStoreConnection pgVectorStoreConnection;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {
      return pgVectorStoreConnection;
  }


  @Override
  public void dispose() {
    try {
      pgVectorStoreConnection.dispose();
    } catch (SQLException ignored) {
      // Exception is already logged in the connection class; do not throw from dispose()
    }

  }

  @Override
  public void initialise()  {
    pgVectorStoreConnection =
        new PGVectorStoreConnection(pgVectorStoreConnectionParameters);
    pgVectorStoreConnection.initialise();
  }

  // Package-private setter for testing
  void setPGVectorStoreConnectionParameters(PGVectorStoreConnectionParameters params) {
    this.pgVectorStoreConnectionParameters = params;
  }
}
