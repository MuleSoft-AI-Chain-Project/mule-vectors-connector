package org.mule.extension.vectors.internal.connection.store.weaviate;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.HttpBasedConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnectionParameters;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("weaviate")
@DisplayName("Weaviate")
@ExternalLib(name = "LangChain4J Weaviate",
    type=DEPENDENCY,
    description = "LangChain4J Weaviate",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-weaviate:1.0.1-beta6")
public class WeaviateStoreConnectionProvider extends HttpBasedConnectionProvider implements
    CachedConnectionProvider<BaseStoreConnection> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeaviateStoreConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private WeaviateStoreConnectionParameters weaviateStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      WeaviateStoreConnection weaviateStoreConnection =
          new WeaviateStoreConnection(weaviateStoreConnectionParameters, getHttpClient());
      return weaviateStoreConnection;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Pinecone", e);
    }
  }



}
