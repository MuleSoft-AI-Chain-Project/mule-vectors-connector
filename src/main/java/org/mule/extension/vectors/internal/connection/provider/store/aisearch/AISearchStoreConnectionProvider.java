package org.mule.extension.vectors.internal.connection.provider.store.aisearch;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.HttpBasedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("aiSearch")
@DisplayName("AI Search")
@ExternalLib(name = "LangChain4J AI Search",
    type=DEPENDENCY,
    description = "LangChain4J AI Search",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-azure-ai-search:1.1.0-beta7")
public class AISearchStoreConnectionProvider extends HttpBasedConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private AISearchStoreConnectionParameters aiSearchStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {
    try {
      AISearchStoreConnection connection = new AISearchStoreConnection(aiSearchStoreConnectionParameters, getHttpClient());
      return connection;
    } catch (Exception e) {
      throw new ConnectionException("Failed to connect to AI Search", e);
    }
  }

}
