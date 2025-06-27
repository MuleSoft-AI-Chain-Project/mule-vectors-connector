package org.mule.extension.vectors.internal.service;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnection;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.aisearch.AISearchStoreServiceProvider;
import org.mule.extension.vectors.internal.store.alloydb.AlloyDBStoreServiceProvider;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.concurrent.ExecutionException;

public class VectorStoreServiceProviderFactory {
  public static VectorStoreServiceProvider getInstance(StoreConfiguration storeConfiguration,
                                               BaseStoreConnection storeConnection,
                                               String storeName,
                                               QueryParameters queryParams,
                                               int dimension,
                                               boolean createStore) throws ExecutionException, InterruptedException {

    switch (storeConnection.getVectorStore()) {


      case Constants.VECTOR_STORE_AI_SEARCH:
        return new AISearchStoreServiceProvider(storeConfiguration, (AISearchStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_ALLOYDB:
        return new AlloyDBStoreServiceProvider( storeConfiguration, (AlloyDBStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);



      default:
        throw new ModuleException(
            String.format("Error while initializing embedding store. \"%s\" not supported.", storeConnection.getVectorStore()),
            MuleVectorsErrorType.STORE_SERVICES_FAILURE);
    }
  }
}
