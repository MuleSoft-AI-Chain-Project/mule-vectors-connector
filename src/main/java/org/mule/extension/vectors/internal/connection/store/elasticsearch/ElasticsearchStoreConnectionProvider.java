package org.mule.extension.vectors.internal.connection.store.elasticsearch;

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

import java.io.IOException;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@Alias("elasticsearch")
@DisplayName("Elasticsearch")
@ExternalLib(name = "LangChain4J Elasticsearch",
    type=DEPENDENCY,
    description = "LangChain4J Elasticsearch",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-elasticsearch:1.1.0-beta7")
public class ElasticsearchStoreConnectionProvider implements
    CachedConnectionProvider<BaseStoreConnection>, BaseStoreConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchStoreConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private ElasticsearchStoreConnectionParameters elasticsearchStoreConnectionParameters;
  private  ElasticsearchStoreConnection elasticsearchStoreConnection;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {


      return elasticsearchStoreConnection;

    }  catch (Exception e) {

      throw new ConnectionException("Failed to connect to Elasticsearch", e);
    }
  }


  @Override
  public void dispose() {
    elasticsearchStoreConnection.disconnect();
  }

  @Override
  public void initialise() throws InitialisationException {
    elasticsearchStoreConnection =
        new ElasticsearchStoreConnection(elasticsearchStoreConnectionParameters);
    try {
      elasticsearchStoreConnection.initialise();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
