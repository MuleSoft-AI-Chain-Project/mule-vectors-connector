package org.mule.extension.vectors.internal.connection.store.opensearch;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("openSearch")
@DisplayName("OpenSearch")
@ExternalLib(name = "LangChain4J OpenSearch",
    type=DEPENDENCY,
    description = "LangChain4J OpenSearch",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-opensearch:1.0.1-beta6")
public class OpenSearchStoreConnectionProvider implements
    CachedConnectionProvider<BaseStoreConnection>, BaseStoreConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpenSearchStoreConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private OpenSearchStoreConnectionParameters openSearchStoreConnectionParameters;
  private  OpenSearchStoreConnection openSearchStoreConnection;

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
      throw new RuntimeException(e);
    }
  }
}
