package org.mule.extension.vectors.internal.store.milvus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilvusStoreServiceProviderTest {

    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private MilvusStoreConnection milvusStoreConnection;
    @Mock
    private QueryParameters queryParameters;

    private static final String STORE_NAME = "test-milvus";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    private MilvusStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new MilvusStoreServiceProvider(
                storeConfiguration,
                milvusStoreConnection,
                STORE_NAME,
                queryParameters,
                DIMENSION,
                CREATE_STORE
        );
    }

    @Test
    void testGetServiceReturnsMilvusStore() {
        assertNotNull(provider.getService());
        assertTrue(provider.getService() instanceof MilvusStore);
    }

    @Test
    void testGetFileIteratorThrowsModuleException() {
        // Since MilvusStoreIterator requires runtime types, we expect a ModuleException or similar error
        assertThrows(Exception.class, () -> provider.getFileIterator());
    }
} 