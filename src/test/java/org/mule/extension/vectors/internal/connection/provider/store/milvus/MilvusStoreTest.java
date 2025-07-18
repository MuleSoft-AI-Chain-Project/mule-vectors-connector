package org.mule.extension.vectors.internal.connection.store.milvus;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.client.MilvusServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.milvus.MilvusStore;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilvusStoreTest {

    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private MilvusStoreConnection milvusStoreConnection;
    @Mock
    private QueryParameters queryParameters;
    @Mock
    private MilvusServiceClient milvusServiceClient;
    @Mock
    private MilvusEmbeddingStore.Builder builder;
    @Mock
    private MilvusEmbeddingStore embeddingStore;

    private static final String STORE_NAME = "test-milvus";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    @BeforeEach
    void setUp() {
        when(milvusStoreConnection.getIndexType()).thenReturn("FLAT");
        when(milvusStoreConnection.getMetricType()).thenReturn("L2");
        when(milvusStoreConnection.getConsistencyLevel()).thenReturn("SESSION");
        when(milvusStoreConnection.isAutoFlushOnInsert()).thenReturn(true);
        when(milvusStoreConnection.getIdFieldName()).thenReturn("id");
        when(milvusStoreConnection.getTextFieldName()).thenReturn("text");
        when(milvusStoreConnection.getMetadataFieldName()).thenReturn("metadata");
        when(milvusStoreConnection.getVectorFieldName()).thenReturn("vector");
        when(milvusStoreConnection.getClient()).thenReturn(milvusServiceClient);
    }

    @Test
    void testBuildEmbeddingStoreReturnsNonNull() {
        try (MockedStatic<MilvusEmbeddingStore> staticMock = mockStatic(MilvusEmbeddingStore.class)) {
            staticMock.when(MilvusEmbeddingStore::builder).thenReturn(builder);
            when(builder.milvusClient(any())).thenReturn(builder);
            when(builder.collectionName(any())).thenReturn(builder);
            when(builder.dimension(anyInt())).thenReturn(builder);
            when(builder.indexType(any())).thenReturn(builder);
            when(builder.metricType(any())).thenReturn(builder);
            when(builder.consistencyLevel(any())).thenReturn(builder);
            when(builder.autoFlushOnInsert(anyBoolean())).thenReturn(builder);
            when(builder.idFieldName(any())).thenReturn(builder);
            when(builder.textFieldName(any())).thenReturn(builder);
            when(builder.metadataFieldName(any())).thenReturn(builder);
            when(builder.vectorFieldName(any())).thenReturn(builder);
            when(builder.build()).thenReturn(embeddingStore);

            MilvusStore store = new MilvusStore(
                    storeConfiguration,
                    milvusStoreConnection,
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
        try (MockedStatic<MilvusEmbeddingStore> staticMock = mockStatic(MilvusEmbeddingStore.class)) {
            staticMock.when(MilvusEmbeddingStore::builder).thenThrow(new RuntimeException("fail"));
            MilvusStore store = new MilvusStore(
                    storeConfiguration,
                    milvusStoreConnection,
                    STORE_NAME,
                    queryParameters,
                    DIMENSION,
                    CREATE_STORE
            );
            assertThrows(ModuleException.class, store::buildEmbeddingStore);
        }
    }
} 
