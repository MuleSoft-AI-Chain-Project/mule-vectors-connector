package org.mule.extension.vectors.internal.store.mongodbatlas;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProvider;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;

public class MongoDBAtlasStoreServiceProvider  implements VectorStoreServiceProvider {

  private final StoreConfiguration storeConfiguration;
  private final MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection;
  private final String storeName;
  private final QueryParameters queryParams;
  private final int dimension;
  private final boolean createStore;

  public MongoDBAtlasStoreServiceProvider(StoreConfiguration compositeConfiguration, MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    this.storeConfiguration = compositeConfiguration;
    this.mongoDBAtlasStoreConnection = mongoDBAtlasStoreConnection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.dimension = dimension;
    this.createStore = createStore;
  }

  @Override
  public VectorStoreService getService() {
    return new MongoDBAtlasStore(storeConfiguration, mongoDBAtlasStoreConnection, storeName, queryParams, dimension, createStore);

  }

  @Override
  public VectoreStoreIterator getFileIterator() {
    return new MongoDBAtlasStoreIterator(mongoDBAtlasStoreConnection, storeName, queryParams);
  }
}
