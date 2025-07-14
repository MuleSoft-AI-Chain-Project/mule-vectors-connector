package org.mule.extension.vectors.internal.service.store.elasticsearch;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.elasticsearch.client.RestClient;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

public class ElasticsearchStore extends BaseStoreService {



  private final RestClient restClient;
  private final QueryParameters queryParams;

  public ElasticsearchStore(StoreConfiguration storeConfiguration, ElasticsearchStoreConnection elasticsearchStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, elasticsearchStoreConnection, storeName, dimension, createStore);
    this.restClient = elasticsearchStoreConnection.getRestClient();
    this.queryParams = queryParams;
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {
    try {
      return ElasticsearchEmbeddingStore.builder()
              .restClient(restClient)
              .indexName(storeName)
              .build();
    } catch (Exception e) {
      throw new ModuleException("Failed to build Elasticsearch embedding store: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  @Override
  public ElasticsearchStoreIterator<?> getFileIterator() {
    return new ElasticsearchStoreIterator<>(
      (org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection) this.storeConnection,
      this.storeName,
      this.queryParams
    );
  }
}
