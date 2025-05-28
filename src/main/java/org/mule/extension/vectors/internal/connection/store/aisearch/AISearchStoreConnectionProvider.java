package org.mule.extension.vectors.internal.connection.store.aisearch;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
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

@Alias("aiSearch")
@DisplayName("AI Search")
@ExternalLib(name = "LangChain4J AI Search",
    type=DEPENDENCY,
    description = "LangChain4J AI Search",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-azure-ai-search:1.0.1-beta6")
public class AISearchStoreConnectionProvider  extends BaseStoreConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(AISearchStoreConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private AISearchStoreConnectionParameters aiSearchStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      AISearchStoreConnection aiSearchStoreConnection =
          new AISearchStoreConnection(aiSearchStoreConnectionParameters.getUrl(),
                                      aiSearchStoreConnectionParameters.getApiKey());
      aiSearchStoreConnection.connect();
      return aiSearchStoreConnection;

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to AI Search", e);
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
        return ConnectionValidationResult.failure("Failed to validate connection to AI Search", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to AI Search", e);
    }
  }
}
