package org.mule.extension.vectors.internal.store.chroma;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.connection.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProvider;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;

public class ChromaStoreServiceProvider implements VectorStoreServiceProvider {

  private final StoreConfiguration storeConfiguration;
  private final ChromaStoreConnection chromaStoreConnection;
  private final String storeName;
  private final QueryParameters queryParams;
  private final int dimension;
  private final boolean createStore;

  public ChromaStoreServiceProvider(StoreConfiguration compositeConfiguration, ChromaStoreConnection chromaStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    this.storeConfiguration = compositeConfiguration;
    this.chromaStoreConnection = chromaStoreConnection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.dimension = dimension;
    this.createStore = createStore;
  }
  @Override
  public VectorStoreService getService() {
    return new ChromaStore(storeConfiguration, chromaStoreConnection, storeName, queryParams, dimension, createStore);
  }

  @Override
  public VectoreStoreIterator getFileIterator() {
    return new ChromaStoreIterator(chromaStoreConnection, storeName, queryParams);
  }
}
