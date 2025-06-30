package org.mule.extension.vectors.internal.store.mongodbatlas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MongoDBAtlasStoreServiceProviderTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection;
    @Mock
    private QueryParameters queryParameters;

    private static final String STORE_NAME = "test-mongo";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    private MongoDBAtlasStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MongoDBAtlasStoreServiceProvider(
                storeConfiguration,
                mongoDBAtlasStoreConnection,
                STORE_NAME,
                queryParameters,
                DIMENSION,
                CREATE_STORE
        );
    }

    @Test
    void testGetServiceReturnsMongoDBAtlasStore() {
        assertNotNull(provider.getService());
        assertTrue(provider.getService() instanceof MongoDBAtlasStore);
    }

    @Test
    void testGetFileIteratorThrowsException() {
        assertThrows(Exception.class, () -> provider.getFileIterator());
    }
} 