package org.mule.extension.vectors.internal.connection.provider.store.elasticsearch;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

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
public class ElasticsearchStoreConnectionProvider implements BaseStoreConnectionProvider {
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
      throw new InitialisationException(e, this);
    }
  }
}
