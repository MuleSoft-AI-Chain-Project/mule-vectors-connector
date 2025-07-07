package org.mule.extension.vectors.internal.connection.store.aisearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.store.aisearch.AISearchStore;
import org.mule.extension.vectors.internal.store.aisearch.AISearchStoreServiceProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AISearchStoreServiceProviderTest {

    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private AISearchStoreConnection aiSearchStoreConnection;
    @Mock
    private QueryParameters queryParameters;

    private static final String STORE_NAME = "test-index";
    private static final int DIMENSION = 3;
    private static final boolean CREATE_STORE = true;

    private AISearchStoreServiceProvider provider;

    @BeforeEach
    void setUp() {
        provider = new AISearchStoreServiceProvider(
                storeConfiguration,
                aiSearchStoreConnection,
                STORE_NAME,
                queryParameters,
                DIMENSION,
                CREATE_STORE
        );
    }

    @Test
    void testGetServiceReturnsAISearchStore() {
        VectorStoreService service = provider.getService();
        assertNotNull(service);
        assertTrue(service instanceof AISearchStore);
    }

    @Test
    void testGetFileIteratorThrowsModuleException() {
        assertThrows(org.mule.runtime.extension.api.exception.ModuleException.class, () -> {
            provider.getFileIterator();
        });
    }
} 
