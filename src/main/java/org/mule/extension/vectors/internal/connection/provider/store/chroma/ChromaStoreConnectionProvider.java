package org.mule.extension.vectors.internal.connection.provider.store.chroma;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.HttpBasedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("chroma")
@DisplayName("Chroma")
@ExternalLib(name = "LangChain4J Chroma",
    type = DEPENDENCY,
    description = "LangChain4J Chroma",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-chroma:1.8.0-beta15")
public class ChromaStoreConnectionProvider extends HttpBasedConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private ChromaStoreConnectionParameters chromaStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      ChromaStoreConnection chromaStoreConnection =
          new ChromaStoreConnection(chromaStoreConnectionParameters, getHttpClient());
      return chromaStoreConnection;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to Chroma", e);
    }
  }
}
