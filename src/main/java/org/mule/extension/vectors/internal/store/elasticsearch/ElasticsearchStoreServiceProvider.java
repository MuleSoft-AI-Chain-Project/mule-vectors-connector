package org.mule.extension.vectors.internal.store.elasticsearch;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProvider;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;

public class ElasticsearchStoreServiceProvider implements VectorStoreServiceProvider {

  private final ElasticsearchStoreConnection elasticsearchStoreConnection;
  private final StoreConfiguration storeConfiguration;
  private final String storeName;
  private final QueryParameters queryParams;
  private final int dimension;
  private final boolean createStore;

  public ElasticsearchStoreServiceProvider(StoreConfiguration compositeConfiguration, ElasticsearchStoreConnection elasticsearchStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    this.storeConfiguration = compositeConfiguration;
    this.elasticsearchStoreConnection = elasticsearchStoreConnection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.dimension = dimension;
    this.createStore = createStore;
  }
  @Override
  public VectorStoreService getService() {
    return new ElasticsearchStore(storeConfiguration,  elasticsearchStoreConnection, storeName, queryParams, dimension, createStore);

  }

  @Override
  public VectoreStoreIterator getFileIterator() {
    return new ElasticsearchStoreIterator(elasticsearchStoreConnection, storeName, queryParams);
  }
}
