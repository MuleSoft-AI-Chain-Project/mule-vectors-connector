package org.mule.extension.vectors.internal.connection.provider.store.qdrant;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("qdrant")
@DisplayName("Qdrant")
@ExternalLib(name = "LangChain4J Qdrant",
    type = DEPENDENCY,
    description = "LangChain4J Qdrant",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-qdrant:1.8.0-beta15")
public class QdrantStoreConnectionProvider implements BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private QdrantStoreConnectionParameters qdrantStoreConnectionParameters;
  private QdrantStoreConnection qdrantStoreConnection;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {



    return qdrantStoreConnection;


  }

  @Override
  public void dispose() {
    qdrantStoreConnection.disconnect();
  }

  @Override
  public void initialise() throws InitialisationException {
    qdrantStoreConnection =
        new QdrantStoreConnection(qdrantStoreConnectionParameters);
    try {
      qdrantStoreConnection.initialise();
    } catch (ConnectionException e) {
      throw new InitialisationException(e, this);
    }
  }

  // Package-private setter for testing
  void setQdrantStoreConnectionParameters(QdrantStoreConnectionParameters params) {
    this.qdrantStoreConnectionParameters = params;
  }
}
