package org.mule.extension.vectors.internal.connection.store.pgvector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.pgvector.PGVectorStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.store.pgvector.PGVectorStore;
import org.mule.extension.vectors.internal.store.pgvector.PGVectorStoreServiceProvider;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PGVectorStoreServiceProviderTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private PGVectorStoreConnection pgVectorStoreConnection;
    @Mock
    private QueryParameters queryParameters;

    private static final String STORE_NAME = "test-pgvector";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    private PGVectorStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new PGVectorStoreServiceProvider(
                storeConfiguration,
                pgVectorStoreConnection,
                STORE_NAME,
                queryParameters,
                DIMENSION,
                CREATE_STORE
        );
    }

    @Test
    void testGetServiceReturnsPGVectorStore() {
        assertNotNull(provider.getService());
        assertTrue(provider.getService() instanceof PGVectorStore);
    }

    @Test
    void testGetFileIteratorThrowsException() {
        assertThrows(Exception.class, () -> provider.getFileIterator());
    }
} 
