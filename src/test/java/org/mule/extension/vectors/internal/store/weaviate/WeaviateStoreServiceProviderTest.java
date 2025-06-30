package org.mule.extension.vectors.internal.store.weaviate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pure JUnit 5 test for WeaviateStoreServiceProvider.
 *
 * Note: WeaviateStoreTest is skipped/deleted because pure unit tests are not feasible without the
 * WeaviateEmbeddingStore.Builder type in the test classpath. This is per project policy for classes
 * requiring unavailable runtime types. See MDC and project documentation for details.
 *
 * getFileIterator() is not unit tested, as it requires Mule runtime types and network dependencies.
 * This is documented and skipped per project policy.
 */
@ExtendWith(MockitoExtension.class)
class WeaviateStoreServiceProviderTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private WeaviateStoreConnection connection;
    @Mock
    private QueryParameters queryParameters;
    private static final String STORE_NAME = "test-store";
    private static final int DIMENSION = 1536;
    private static final boolean CREATE_STORE = true;
    private WeaviateStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new WeaviateStoreServiceProvider(storeConfiguration, connection, STORE_NAME, queryParameters, DIMENSION, CREATE_STORE);
    }

    @Test
    void testGetServiceReturnsWeaviateStore() {
        VectorStoreService service = provider.getService();
        assertNotNull(service);
        assertTrue(service instanceof WeaviateStore);
    }

    // getFileIterator() is not unit tested due to runtime dependencies (see class-level Javadoc).
} 