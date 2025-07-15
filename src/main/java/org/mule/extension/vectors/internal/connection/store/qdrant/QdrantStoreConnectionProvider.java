package org.mule.extension.vectors.internal.connection.store.qdrant;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("qdrant")
@DisplayName("Qdrant")
@ExternalLib(name = "LangChain4J Qdrant",
    type=DEPENDENCY,
    description = "LangChain4J Qdrant",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-qdrant:1.1.0-beta7")
public class QdrantStoreConnectionProvider implements
    CachedConnectionProvider<BaseStoreConnection>, BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private QdrantStoreConnectionParameters qdrantStoreConnectionParameters;
  private  QdrantStoreConnection qdrantStoreConnection;

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
      throw new RuntimeException(e);
    }
  }

  // Package-private setter for testing
  void setQdrantStoreConnectionParameters(QdrantStoreConnectionParameters params) {
    this.qdrantStoreConnectionParameters = params;
  }
}
