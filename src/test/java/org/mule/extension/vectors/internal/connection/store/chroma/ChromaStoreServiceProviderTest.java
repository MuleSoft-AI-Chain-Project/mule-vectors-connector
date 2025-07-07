package org.mule.extension.vectors.internal.connection.store.chroma;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.store.chroma.ChromaStore;
import org.mule.extension.vectors.internal.store.chroma.ChromaStoreServiceProvider;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ChromaStoreServiceProviderTest {

    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private ChromaStoreConnection chromaStoreConnection;
    @Mock
    private QueryParameters queryParameters;

    private static final String STORE_NAME = "test-chroma";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    private ChromaStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ChromaStoreServiceProvider(
                storeConfiguration,
                chromaStoreConnection,
                STORE_NAME,
                queryParameters,
                DIMENSION,
                CREATE_STORE
        );
    }

    @Test
    void testGetServiceReturnsChromaStore() {
        assertNotNull(provider.getService());
        assertTrue(provider.getService() instanceof ChromaStore);
    }

    @Test
    void testGetFileIteratorThrowsException() {
        assertThrows(Exception.class, () -> provider.getFileIterator());
    }
} 
