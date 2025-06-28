package org.mule.extension.vectors.internal.store.ephemeralfile;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.connection.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProvider;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;

public class EphemeralFileStoreServiceProvider implements VectorStoreServiceProvider {

  private final StoreConfiguration storeConfiguration;
  private final EphemeralFileStoreConnection connection;
  private final String storeName;
  private final QueryParameters queryParams;
  private final int dimension;
  private final boolean createStore;

  public EphemeralFileStoreServiceProvider(StoreConfiguration compositeConfiguration, EphemeralFileStoreConnection connection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    this.storeConfiguration = compositeConfiguration;
    this.connection = connection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.dimension = dimension;
    this.createStore = createStore;
  }

  @Override
  public VectorStoreService getService() {
    return new EphemeralFileStore(storeConfiguration,connection, storeName, queryParams, dimension, createStore);

  }

  @Override
  public VectoreStoreIterator getFileIterator() {
    return new EphemeralFileStoreIterator(connection, queryParams);
  }
}
