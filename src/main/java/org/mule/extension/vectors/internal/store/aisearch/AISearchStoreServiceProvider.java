package org.mule.extension.vectors.internal.store.aisearch;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProvider;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;


public class AISearchStoreServiceProvider implements VectorStoreServiceProvider {

  private final StoreConfiguration storeConfiguration;
  private final AISearchStoreConnection aiSearchStoreConnection;
  private final String storeName;
  private final QueryParameters queryParams;
  private final int dimension;
  private final boolean createStore;

  public AISearchStoreServiceProvider(StoreConfiguration compositeConfiguration, AISearchStoreConnection aiSearchStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    this.storeConfiguration = compositeConfiguration;
    this.aiSearchStoreConnection = aiSearchStoreConnection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.dimension = dimension;
    this.createStore = createStore;
  }
  @Override
  public VectorStoreService getService() {
    return new AISearchStore(storeConfiguration, aiSearchStoreConnection, storeName, queryParams, dimension, createStore);
  }

  @Override
  public VectoreStoreIterator getFileIterator() {
    return new AISearchStoreIterator(storeName, queryParams, aiSearchStoreConnection, dimension);
  }
}
