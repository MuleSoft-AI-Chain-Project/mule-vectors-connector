package org.mule.extension.vectors.internal.store.opensearch;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenSearchStoreTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private OpenSearchStoreConnection openSearchStoreConnection;
    @Mock
    private QueryParameters queryParameters;
    @Mock
    private OpenSearchClient openSearchClient;
    @Mock
    private OpenSearchEmbeddingStore.Builder builder;
    @Mock
    private OpenSearchEmbeddingStore embeddingStore;

    private static final String STORE_NAME = "test-opensearch";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    @BeforeEach
    void setUp() {
        when(openSearchStoreConnection.getUrl()).thenReturn("http://localhost:9200");
        when(openSearchStoreConnection.getOpenSearchClient()).thenReturn(openSearchClient);
    }

    @Test
    void testBuildEmbeddingStoreReturnsNonNull() {
        try (MockedStatic<OpenSearchEmbeddingStore> staticMock = mockStatic(OpenSearchEmbeddingStore.class)) {
            staticMock.when(OpenSearchEmbeddingStore::builder).thenReturn(builder);
            when(builder.serverUrl(any())).thenReturn(builder);
            when(builder.openSearchClient(any())).thenReturn(builder);
            when(builder.indexName(any())).thenReturn(builder);
            when(builder.build()).thenReturn(embeddingStore);

            OpenSearchStore store = new OpenSearchStore(
                    storeConfiguration,
                    openSearchStoreConnection,
                    STORE_NAME,
                    queryParameters,
                    DIMENSION,
                    CREATE_STORE
            );
            EmbeddingStore<TextSegment> result = store.buildEmbeddingStore();
            assertNotNull(result);
        }
    }

    @Test
    void testBuildEmbeddingStoreThrowsModuleExceptionOnError() {
        try (MockedStatic<OpenSearchEmbeddingStore> staticMock = mockStatic(OpenSearchEmbeddingStore.class)) {
            staticMock.when(OpenSearchEmbeddingStore::builder).thenThrow(new RuntimeException("fail"));
            OpenSearchStore store = new OpenSearchStore(
                    storeConfiguration,
                    openSearchStoreConnection,
                    STORE_NAME,
                    queryParameters,
                    DIMENSION,
                    CREATE_STORE
            );
            assertThrows(ModuleException.class, store::buildEmbeddingStore);
        }
    }
} 