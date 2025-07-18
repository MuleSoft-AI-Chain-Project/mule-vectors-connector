package org.mule.extension.vectors.internal.store.qdrant;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.qdrant.QdrantStore;
import org.mule.runtime.extension.api.exception.ModuleException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.common.util.concurrent.Futures;

@ExtendWith(MockitoExtension.class)
class QdrantStoreTest {
    @Mock StoreConfiguration storeConfiguration;
    @Mock QdrantStoreConnection qdrantStoreConnection;
    @Mock QdrantClient qdrantClient;
    @Mock QueryParameters queryParameters;
    @Mock QdrantEmbeddingStore.Builder embeddingStoreBuilder;
    @Mock QdrantEmbeddingStore embeddingStore;

    @BeforeEach
    void setUp() {
        lenient().when(qdrantStoreConnection.getClient()).thenReturn(qdrantClient);
        lenient().when(qdrantStoreConnection.getTextSegmentKey()).thenReturn("text");
        lenient().when(queryParameters.pageSize()).thenReturn(2);
        lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
    }

    @Test
    void constructor_createsCollectionIfNotExists() throws Exception {
        when(qdrantClient.collectionExistsAsync(anyString())).thenReturn(Futures.immediateFuture(false));
        doNothing().when(qdrantStoreConnection).createCollection(anyString(), anyInt());
        QdrantStore store = new QdrantStore(storeConfiguration, qdrantStoreConnection, "testStore", queryParameters, 128, true);
        // Should call createCollection
        verify(qdrantStoreConnection).createCollection(eq("testStore"), eq(128));
    }

    @Test
    void constructor_doesNotCreateCollectionIfExists() throws Exception {
        when(qdrantClient.collectionExistsAsync(anyString())).thenReturn(Futures.immediateFuture(true));
        QdrantStore store = new QdrantStore(storeConfiguration, qdrantStoreConnection, "testStore", queryParameters, 128, true);
        verify(qdrantStoreConnection, never()).createCollection(anyString(), anyInt());
    }

    @Test
    void constructor_throwsModuleExceptionOnError() throws Exception {
        when(qdrantClient.collectionExistsAsync(anyString())).thenThrow(new RuntimeException("fail"));
        assertThatThrownBy(() -> new QdrantStore(storeConfiguration, qdrantStoreConnection, "testStore", queryParameters, 128, true))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Qdrant API request failed");
    }

    @Test
    void buildEmbeddingStore_returnsQdrantEmbeddingStore() {
        // Use real QdrantStore, but mock the builder static method
        try (var builderMocked = mockStatic(QdrantEmbeddingStore.class)) {
            builderMocked.when(QdrantEmbeddingStore::builder).thenReturn(embeddingStoreBuilder);
            when(embeddingStoreBuilder.client(qdrantClient)).thenReturn(embeddingStoreBuilder);
            when(embeddingStoreBuilder.payloadTextKey("text")).thenReturn(embeddingStoreBuilder);
            when(embeddingStoreBuilder.collectionName("testStore")).thenReturn(embeddingStoreBuilder);
            when(embeddingStoreBuilder.build()).thenReturn(embeddingStore);
            when(qdrantClient.collectionExistsAsync(anyString())).thenReturn(Futures.immediateFuture(true));
            QdrantStore store = new QdrantStore(storeConfiguration, qdrantStoreConnection, "testStore", queryParameters, 128, true);
            EmbeddingStore<TextSegment> result = store.buildEmbeddingStore();
            assertThat(result).isSameAs(embeddingStore);
        }
    }

    @Test
    void buildEmbeddingStore_throwsModuleExceptionOnError() {
        try (var builderMocked = mockStatic(QdrantEmbeddingStore.class)) {
            builderMocked.when(QdrantEmbeddingStore::builder).thenThrow(new RuntimeException("fail"));
            when(qdrantClient.collectionExistsAsync(anyString())).thenReturn(Futures.immediateFuture(true));
            QdrantStore store = new QdrantStore(storeConfiguration, qdrantStoreConnection, "testStore", queryParameters, 128, true);
            assertThatThrownBy(store::buildEmbeddingStore)
                    .isInstanceOf(ModuleException.class)
                    .hasMessageContaining("Failed to build Qdrant embedding store");
        }
    }
}
