package org.mule.extension.vectors.internal.connection.store.elasticsearch;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionProvider;
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

@Alias("elasticsearch")
@DisplayName("Elasticsearch")
@ExternalLib(name = "LangChain4J Elasticsearch",
    type=DEPENDENCY,
    description = "LangChain4J Elasticsearch",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore",
    coordinates = "dev.langchain4j:langchain4j-elasticsearch:1.0.1-beta6")
public class ElasticsearchStoreConnectionProvider  implements BaseStoreConnectionProvider,
    CachedConnectionProvider<BaseStoreConnection> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchStoreConnectionProvider.class);

  @ParameterGroup(name = Placement.CONNECTION_TAB)
  private ElasticsearchStoreConnectionParameters elasticsearchStoreConnectionParameters;

  @Override
  public BaseStoreConnection connect() throws ConnectionException {

    try {

      ElasticsearchStoreConnection elasticsearchStoreConnection =
          new ElasticsearchStoreConnection(elasticsearchStoreConnectionParameters);
      return elasticsearchStoreConnection;

    }  catch (Exception e) {

      throw new ConnectionException("Failed to connect to Elasticsearch", e);
    }
  }


}
