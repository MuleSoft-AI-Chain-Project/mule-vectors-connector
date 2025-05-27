package org.mule.extension.vectors.internal.connection.store.opensearch;

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

@Alias("openSearch")
@DisplayName("OpenSearch")
@ExternalLib(name = "LangChain4J OpenSearch",
    type=DEPENDENCY,
    description = "LangChain4J OpenSearch",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-opensearch:1.0.1-beta6")
public class OpenSearchStoreConnectionProvider  extends BaseStoreConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenSearchStoreConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private OpenSearchStoreConnectionParameters openSearchStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      OpenSearchStoreConnection openSearchStoreConnection =
          new OpenSearchStoreConnection(openSearchStoreConnectionParameters.getUrl(),
                                        openSearchStoreConnectionParameters.getUser(),
                                        openSearchStoreConnectionParameters.getPassword(),
                                        openSearchStoreConnectionParameters.getApiKey());
      openSearchStoreConnection.connect();
      return openSearchStoreConnection;

    } catch (ConnectionException e) {

      throw e;

    } catch (Exception e) {

      throw new ConnectionException("Failed to connect to OpenSearch", e);
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
        return ConnectionValidationResult.failure("Failed to validate connection to OpenSearch", null);
      }
    } catch (Exception e) {
      return ConnectionValidationResult.failure("Failed to validate connection to OpenSearch", e);
    }
  }
}
