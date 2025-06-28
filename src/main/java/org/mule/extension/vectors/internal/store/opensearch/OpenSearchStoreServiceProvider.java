package org.mule.extension.vectors.internal.store.opensearch;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.connection.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProvider;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;

public class OpenSearchStoreServiceProvider implements VectorStoreServiceProvider {

  private final StoreConfiguration storeConfiguration;
  private final OpenSearchStoreConnection openSearchStoreConnection;
  private final String storeName;
  private final QueryParameters queryParams;
  private final int dimension;
  private final boolean createStore;

  public OpenSearchStoreServiceProvider(StoreConfiguration compositeConfiguration, OpenSearchStoreConnection openSearchStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    this.storeConfiguration = compositeConfiguration;
    this.openSearchStoreConnection = openSearchStoreConnection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.dimension = dimension;
    this.createStore = createStore;
  }

  @Override
  public VectorStoreService getService() {
    return new OpenSearchStore(storeConfiguration, openSearchStoreConnection, storeName, queryParams, dimension, createStore);
  }

  @Override
  public VectoreStoreIterator getFileIterator() {
    return new OpenSearchStoreIterator(openSearchStoreConnection, storeName, queryParams);
  }
}
