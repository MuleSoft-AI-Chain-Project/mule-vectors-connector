package org.mule.extension.vectors.internal.store.alloydb;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProvider;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;
import org.mule.extension.vectors.internal.store.aisearch.AISearchStoreIterator;

public class AlloyDBStoreServiceProvider implements VectorStoreServiceProvider {

  private final StoreConfiguration storeConfiguration;
  private final AlloyDBStoreConnection alloyDBStoreConnection;
  private final String storeName;
  private final QueryParameters queryParams;
  private final int dimension;
  private final boolean createStore;

  public AlloyDBStoreServiceProvider(StoreConfiguration compositeConfiguration, AlloyDBStoreConnection alloyDBStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    this.storeConfiguration = compositeConfiguration;
    this.alloyDBStoreConnection = alloyDBStoreConnection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.dimension = dimension;
    this.createStore = createStore;
  }
  @Override
  public VectorStoreService getService() {
    return new AlloyDBStore(storeConfiguration, alloyDBStoreConnection, storeName, queryParams, dimension, createStore);
  }

  @Override
  public VectoreStoreIterator getFileIterator() {
    return new AlloyDBStoreIterator( alloyDBStoreConnection, storeName, queryParams);
  }
}
