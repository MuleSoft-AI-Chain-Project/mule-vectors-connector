package org.mule.extension.vectors.internal.connection.store.pinecone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.store.pinecone.PineconeStore;
import org.mule.extension.vectors.internal.store.pinecone.PineconeStoreServiceProvider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure JUnit 5 test for PineconeStoreServiceProvider.
 * getFileIterator() is not unit tested due to runtime dependencies (see project MDC policy).
 */
@ExtendWith(MockitoExtension.class)
class PineconeStoreServiceProviderTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private PineconeStoreConnection pineconeStoreConnection;
    @Mock
    private QueryParameters queryParameters;

    private static final String STORE_NAME = "test-pinecone";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    private PineconeStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new PineconeStoreServiceProvider(
                storeConfiguration,
                pineconeStoreConnection,
                STORE_NAME,
                queryParameters,
                DIMENSION,
                CREATE_STORE
        );
    }

    @Test
    void testGetServiceReturnsPineconeStore() {
        assertNotNull(provider.getService());
        assertTrue(provider.getService() instanceof PineconeStore);
    }

    // Skipped: getFileIterator() requires runtime types and cannot be unit tested with pure JUnit 5.
} 
