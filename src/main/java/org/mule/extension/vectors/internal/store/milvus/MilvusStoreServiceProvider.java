package org.mule.extension.vectors.internal.store.milvus;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProvider;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;

public class MilvusStoreServiceProvider  implements VectorStoreServiceProvider {

  private final StoreConfiguration storeConfiguration;
  private final MilvusStoreConnection milvusStoreConnection;
  private final String storeName;
  private final QueryParameters queryParams;
  private final int dimension;
  private final boolean createStore;

  public MilvusStoreServiceProvider(StoreConfiguration compositeConfiguration, MilvusStoreConnection milvusStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    this.storeConfiguration = compositeConfiguration;
    this.milvusStoreConnection = milvusStoreConnection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.dimension = dimension;
    this.createStore = createStore;
  }

  @Override
  public VectorStoreService getService() {
    return new MilvusStore(storeConfiguration, milvusStoreConnection, storeName, queryParams, dimension, createStore);

  }

  @Override
  public VectoreStoreIterator getFileIterator() {
    return new MilvusStoreIterator(milvusStoreConnection,storeName,queryParams);
  }
}
