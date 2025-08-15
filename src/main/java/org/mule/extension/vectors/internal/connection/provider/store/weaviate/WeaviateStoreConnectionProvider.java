package org.mule.extension.vectors.internal.connection.provider.store.weaviate;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.HttpBasedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("weaviate")
@DisplayName("Weaviate")
@ExternalLib(name = "LangChain4J Weaviate",
    type = DEPENDENCY,
    description = "LangChain4J Weaviate",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-weaviate:1.1.0-beta7")
public class WeaviateStoreConnectionProvider extends HttpBasedConnectionProvider {

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
