package org.mule.extension.vectors.internal.connection.store.opensearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.store.opensearch.OpenSearchStore;
import org.mule.extension.vectors.internal.store.opensearch.OpenSearchStoreServiceProvider;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenSearchStoreServiceProviderTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private OpenSearchStoreConnection openSearchStoreConnection;
    @Mock
    private QueryParameters queryParameters;

    private static final String STORE_NAME = "test-opensearch";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    private OpenSearchStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new OpenSearchStoreServiceProvider(
                storeConfiguration,
                openSearchStoreConnection,
                STORE_NAME,
                queryParameters,
                DIMENSION,
                CREATE_STORE
        );
    }

    @Test
    void testGetServiceReturnsOpenSearchStore() {
        assertNotNull(provider.getService());
        assertTrue(provider.getService() instanceof OpenSearchStore);
    }

    @Test
    void testGetFileIteratorThrowsException() {
        assertThrows(Exception.class, () -> provider.getFileIterator());
    }
} 
