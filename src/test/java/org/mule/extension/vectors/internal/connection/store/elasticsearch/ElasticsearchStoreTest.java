package org.mule.extension.vectors.internal.connection.store.elasticsearch;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.elasticsearch.ElasticsearchStore;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElasticsearchStoreTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private ElasticsearchStoreConnection elasticsearchStoreConnection;
    @Mock
    private QueryParameters queryParameters;
    @Mock
    private RestClient restClient;
    @Mock
    private ElasticsearchEmbeddingStore.Builder builder;
    @Mock
    private ElasticsearchEmbeddingStore embeddingStore;

    private static final String STORE_NAME = "test-elastic";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    @BeforeEach
    void setUp() {
        when(elasticsearchStoreConnection.getRestClient()).thenReturn(restClient);
    }

    @Test
    void testBuildEmbeddingStoreReturnsNonNull() {
        try (MockedStatic<ElasticsearchEmbeddingStore> staticMock = mockStatic(ElasticsearchEmbeddingStore.class)) {
            staticMock.when(ElasticsearchEmbeddingStore::builder).thenReturn(builder);
            when(builder.restClient(any())).thenReturn(builder);
            when(builder.indexName(any())).thenReturn(builder);
            when(builder.build()).thenReturn(embeddingStore);

            ElasticsearchStore store = new ElasticsearchStore(
                    storeConfiguration,
                    elasticsearchStoreConnection,
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
        try (MockedStatic<ElasticsearchEmbeddingStore> staticMock = mockStatic(ElasticsearchEmbeddingStore.class)) {
            staticMock.when(ElasticsearchEmbeddingStore::builder).thenThrow(new RuntimeException("fail"));
            ElasticsearchStore store = new ElasticsearchStore(
                    storeConfiguration,
                    elasticsearchStoreConnection,
                    STORE_NAME,
                    queryParameters,
                    DIMENSION,
                    CREATE_STORE
            );
            assertThrows(ModuleException.class, store::buildEmbeddingStore);
        }
    }
} 
