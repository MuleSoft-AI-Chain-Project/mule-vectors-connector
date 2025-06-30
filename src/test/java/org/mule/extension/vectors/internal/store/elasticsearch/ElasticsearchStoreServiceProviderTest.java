package org.mule.extension.vectors.internal.store.elasticsearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ElasticsearchStoreServiceProviderTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private ElasticsearchStoreConnection elasticsearchStoreConnection;
    @Mock
    private QueryParameters queryParameters;

    private static final String STORE_NAME = "test-elastic";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    private ElasticsearchStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ElasticsearchStoreServiceProvider(
                storeConfiguration,
                elasticsearchStoreConnection,
                STORE_NAME,
                queryParameters,
                DIMENSION,
                CREATE_STORE
        );
    }

    @Test
    void testGetServiceReturnsElasticsearchStore() {
        assertNotNull(provider.getService());
        assertTrue(provider.getService() instanceof ElasticsearchStore);
    }

    @Test
    void testGetFileIteratorThrowsException() {
        assertThrows(Exception.class, () -> provider.getFileIterator());
    }
} 