package org.mule.extension.vectors.internal.connection.store.qdrant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Futures;
import io.qdrant.client.QdrantClient;
import org.mule.extension.vectors.internal.store.qdrant.QdrantStore;
import org.mule.extension.vectors.internal.store.qdrant.QdrantStoreServiceProvider;

import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure JUnit 5 test for QdrantStoreServiceProvider.
 * All dependencies are mocked. Iterator test is skipped per project policy.
 */
@ExtendWith(MockitoExtension.class)
class QdrantStoreServiceProviderTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private QdrantStoreConnection connection;
    @Mock
    private QueryParameters queryParameters;
    @Mock
    private QdrantClient qdrantClient;

    private static final String STORE_NAME = "test-qdrant";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    private QdrantStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        when(connection.getClient()).thenReturn(qdrantClient);
        when(connection.getTextSegmentKey()).thenReturn("text");
        ListenableFuture<Boolean> future = Futures.immediateFuture(true);
        when(qdrantClient.collectionExistsAsync(STORE_NAME)).thenReturn(future);
        provider = new QdrantStoreServiceProvider(storeConfiguration, connection, STORE_NAME, queryParameters, DIMENSION, CREATE_STORE);
    }

    @Test
    void testGetServiceReturnsQdrantStore() {
        assertNotNull(provider.getService());
        assertTrue(provider.getService() instanceof QdrantStore);
    }

    // Skipped: getFileIterator() requires runtime types and network, not feasible for pure unit test.
} 
