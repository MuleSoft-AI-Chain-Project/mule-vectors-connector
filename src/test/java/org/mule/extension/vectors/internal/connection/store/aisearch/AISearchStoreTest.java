package org.mule.extension.vectors.internal.connection.store.aisearch;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.aisearch.AISearchStore;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AISearchStoreTest {

    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private QueryParameters queryParameters;

    private AISearchStore aisearchStore;
    private AISearchStoreConnection aiSearchStoreConnection;

    @BeforeEach
    void setUp() {
        // Create dummy connection parameters
        AISearchStoreConnectionParameters params = new AISearchStoreConnectionParameters() {
            @Override public String getUrl() { return "https://test-url"; }
            @Override public String getApiKey() { return "test-api-key"; }
        };
        aiSearchStoreConnection = new AISearchStoreConnection(params, null); // null HttpClient for pure unit test
        aisearchStore = new AISearchStore(storeConfiguration, aiSearchStoreConnection, "test-index", queryParameters, 1536, false);
    }

    @Test
    void shouldBuildEmbeddingStoreSuccessfully() {
        EmbeddingStore<?> store = aisearchStore.buildEmbeddingStore();
        assertNotNull(store);
        assertTrue(store instanceof AzureAiSearchEmbeddingStore);
    }

    @Test
    void shouldThrowModuleExceptionWhenDimensionIsZeroAndCreateStoreTrue() {
        aisearchStore = new AISearchStore(storeConfiguration, aiSearchStoreConnection, "test-index", queryParameters, 0, true);
        assertThrows(ModuleException.class, () -> aisearchStore.buildEmbeddingStore());
    }
}
