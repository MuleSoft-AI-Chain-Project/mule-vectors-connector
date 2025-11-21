package org.mule.extension.vectors.internal.connection.provider.store.opensearch;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionProvider;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.net.URISyntaxException;

@Alias("openSearch")
@DisplayName("OpenSearch")
@ExternalLib(name = "LangChain4J OpenSearch",
    type = DEPENDENCY,
    description = "LangChain4J OpenSearch",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-opensearch:1.8.0-beta15")
public class OpenSearchStoreConnectionProvider implements BaseStoreConnectionProvider {

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private OpenSearchStoreConnectionParameters openSearchStoreConnectionParameters;
  private OpenSearchStoreConnection openSearchStoreConnection;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {
    return openSearchStoreConnection;
  }

  @Override
  public void dispose() {
    openSearchStoreConnection.disconnect();
  }

  @Override
  public void initialise() throws InitialisationException {
    openSearchStoreConnection =
        new OpenSearchStoreConnection(openSearchStoreConnectionParameters);
    try {
      openSearchStoreConnection.initialise();
    } catch (URISyntaxException e) {
      throw new ModuleException("Failed to initialize OpenSearch connection: " + e.getMessage(),
                                MuleVectorsErrorType.STORE_CONNECTION_FAILURE, e);
    }
  }
}
