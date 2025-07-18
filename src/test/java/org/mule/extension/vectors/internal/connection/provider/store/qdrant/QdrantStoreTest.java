package org.mule.extension.vectors.internal.connection.store.qdrant;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.qdrant.QdrantStore;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Futures;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QdrantStoreTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private QdrantStoreConnection qdrantStoreConnection;
    @Mock
    private QdrantClient qdrantClient;
    @Mock
    private QueryParameters queryParameters;
    @Mock
    private QdrantEmbeddingStore.Builder builder;
    @Mock
    private QdrantEmbeddingStore embeddingStore;

    private static final String STORE_NAME = "test-qdrant";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;
    private static final boolean NO_CREATE_STORE = false;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(qdrantStoreConnection.getClient()).thenReturn(qdrantClient);
        lenient().when(qdrantStoreConnection.getTextSegmentKey()).thenReturn("text");
        lenient().when(queryParameters.pageSize()).thenReturn(10);
    }

    @Test
    void testBuildEmbeddingStoreWithCreateStoreTrue() throws Exception {
        ListenableFuture<Boolean> future = Futures.immediateFuture(false);
        when(qdrantClient.collectionExistsAsync(STORE_NAME)).thenReturn(future);
        doNothing().when(qdrantStoreConnection).createCollection(STORE_NAME, DIMENSION);

        try (MockedStatic<QdrantEmbeddingStore> qdrantStatic = mockStatic(QdrantEmbeddingStore.class)) {
            qdrantStatic.when(QdrantEmbeddingStore::builder).thenReturn(builder);
            when(builder.client(qdrantClient)).thenReturn(builder);
            when(builder.payloadTextKey(anyString())).thenReturn(builder);
            when(builder.collectionName(anyString())).thenReturn(builder);
            when(builder.build()).thenReturn(embeddingStore);

            QdrantStore store = new QdrantStore(storeConfiguration, qdrantStoreConnection, STORE_NAME, queryParameters, DIMENSION, CREATE_STORE);
            EmbeddingStore<?> result = store.buildEmbeddingStore();
            assertNotNull(result);
            assertEquals(embeddingStore, result);
        }
    }

    @Test
    void testBuildEmbeddingStoreWithCreateStoreFalse() throws Exception {
        ListenableFuture<Boolean> future = Futures.immediateFuture(true);
        lenient().when(qdrantClient.collectionExistsAsync(STORE_NAME)).thenReturn(future);

        try (MockedStatic<QdrantEmbeddingStore> qdrantStatic = mockStatic(QdrantEmbeddingStore.class)) {
            qdrantStatic.when(QdrantEmbeddingStore::builder).thenReturn(builder);
            when(builder.client(qdrantClient)).thenReturn(builder);
            when(builder.payloadTextKey(anyString())).thenReturn(builder);
            when(builder.collectionName(anyString())).thenReturn(builder);
            when(builder.build()).thenReturn(embeddingStore);

            QdrantStore store = new QdrantStore(storeConfiguration, qdrantStoreConnection, STORE_NAME, queryParameters, DIMENSION, NO_CREATE_STORE);
            EmbeddingStore<?> result = store.buildEmbeddingStore();
            assertNotNull(result);
            assertEquals(embeddingStore, result);
        }
    }

    @Test
    void testConstructorThrowsModuleExceptionOnError() throws Exception {
        ListenableFuture<Boolean> future = Futures.immediateFailedFuture(new RuntimeException("fail"));
        lenient().when(qdrantClient.collectionExistsAsync(STORE_NAME)).thenReturn(future);
        lenient().when(qdrantStoreConnection.getTextSegmentKey()).thenReturn("text");
        assertThrows(ModuleException.class, () -> new QdrantStore(storeConfiguration, qdrantStoreConnection, STORE_NAME, queryParameters, DIMENSION, CREATE_STORE));
    }

    @Test
    void testBuildEmbeddingStoreThrowsModuleExceptionOnError() throws Exception {
        ListenableFuture<Boolean> future = Futures.immediateFuture(true);
        lenient().when(qdrantClient.collectionExistsAsync(STORE_NAME)).thenReturn(future);
        try (MockedStatic<QdrantEmbeddingStore> qdrantStatic = mockStatic(QdrantEmbeddingStore.class)) {
            qdrantStatic.when(QdrantEmbeddingStore::builder).thenReturn(builder);
            when(builder.client(qdrantClient)).thenReturn(builder);
            when(builder.payloadTextKey(anyString())).thenReturn(builder);
            when(builder.collectionName(anyString())).thenReturn(builder);
            when(builder.build()).thenThrow(new RuntimeException("fail"));

            QdrantStore store = new QdrantStore(storeConfiguration, qdrantStoreConnection, STORE_NAME, queryParameters, DIMENSION, NO_CREATE_STORE);
            assertThrows(ModuleException.class, store::buildEmbeddingStore);
        }
    }
} 
