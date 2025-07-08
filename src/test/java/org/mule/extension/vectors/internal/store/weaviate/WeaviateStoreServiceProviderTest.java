package org.mule.extension.vectors.internal.store.weaviate;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;

import static org.junit.jupiter.api.Assertions.*;

class WeaviateStoreServiceProviderTest {
    @Test
    void constructionAndGetters() {
        StoreConfiguration config = new StoreConfiguration();
        WeaviateStoreConnection conn = null; // Not used in provider
        String storeName = "testStore";
        QueryParameters queryParams = new QueryParameters();
        int dimension = 128;
        boolean createStore = true;
        WeaviateStoreServiceProvider provider = new WeaviateStoreServiceProvider(config, conn, storeName, queryParams, dimension, createStore);
        assertNotNull(provider);
        assertTrue(provider.getService() instanceof WeaviateStore);
        assertTrue(provider.getFileIterator() instanceof WeaviateStoreIterator);
    }
} 