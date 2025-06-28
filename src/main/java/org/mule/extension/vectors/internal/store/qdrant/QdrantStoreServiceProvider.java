package org.mule.extension.vectors.internal.store.qdrant;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.connection.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProvider;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;

public class QdrantStoreServiceProvider implements VectorStoreServiceProvider {

  private final StoreConfiguration storeConfiguration;
  private final QdrantStoreConnection connection;
  private final String storeName;
  private final QueryParameters queryParams;
  private final int dimension;
  private final boolean createStore;

  public QdrantStoreServiceProvider(StoreConfiguration compositeConfiguration, QdrantStoreConnection connection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    this.storeConfiguration = compositeConfiguration;
    this.connection = connection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.dimension = dimension;
    this.createStore = createStore;
  }

  @Override
  public VectorStoreService getService() {
    return new QdrantStore(storeConfiguration,  connection, storeName, queryParams, dimension, createStore);

  }

  @Override
  public VectoreStoreIterator getFileIterator() {
    return new QdrantStoreIterator(connection, storeName, queryParams);
  }
}
