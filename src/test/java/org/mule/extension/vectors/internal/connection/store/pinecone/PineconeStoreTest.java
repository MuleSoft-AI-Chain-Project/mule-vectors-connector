package org.mule.extension.vectors.internal.connection.store.pinecone;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.store.pinecone.PineconeStore;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PineconeStoreTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private PineconeStoreConnection pineconeStoreConnection;
    @Mock
    private QueryParameters queryParameters;
    @Mock
    private PineconeEmbeddingStore.Builder builder;
    @Mock
    private PineconeEmbeddingStore embeddingStore;
    @Mock
    private PineconeServerlessIndexConfig.Builder indexConfigBuilder;
    @Mock
    private PineconeServerlessIndexConfig indexConfig;

    private static final String STORE_NAME = "test-pinecone";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;
    private static final boolean NO_CREATE_STORE = false;

    @BeforeEach
    void setUp() {
        when(pineconeStoreConnection.getApiKey()).thenReturn("api-key");
        when(pineconeStoreConnection.getCloud()).thenReturn("cloud");
        when(pineconeStoreConnection.getRegion()).thenReturn("region");
        when(queryParameters.pageSize()).thenReturn(10);
    }

    @Test
    void testBuildEmbeddingStoreWithCreateStoreTrue() {
        try (MockedStatic<PineconeEmbeddingStore> pineconeStatic = mockStatic(PineconeEmbeddingStore.class);
             MockedStatic<PineconeServerlessIndexConfig> indexConfigStatic = mockStatic(PineconeServerlessIndexConfig.class)) {

            pineconeStatic.when(PineconeEmbeddingStore::builder).thenReturn(builder);
            when(builder.apiKey(anyString())).thenReturn(builder);
            when(builder.index(anyString())).thenReturn(builder);
            when(builder.nameSpace(anyString())).thenReturn(builder);
            indexConfigStatic.when(PineconeServerlessIndexConfig::builder).thenReturn(indexConfigBuilder);
            when(indexConfigBuilder.cloud(anyString())).thenReturn(indexConfigBuilder);
            when(indexConfigBuilder.region(anyString())).thenReturn(indexConfigBuilder);
            when(indexConfigBuilder.dimension(anyInt())).thenReturn(indexConfigBuilder);
            when(indexConfigBuilder.build()).thenReturn(indexConfig);
            when(builder.createIndex(indexConfig)).thenReturn(builder);
            when(builder.build()).thenReturn(embeddingStore);

            PineconeStore store = new PineconeStore(storeConfiguration, pineconeStoreConnection, STORE_NAME, queryParameters, DIMENSION, CREATE_STORE);
            EmbeddingStore<?> result = store.buildEmbeddingStore();
            assertNotNull(result);
            assertEquals(embeddingStore, result);
        }
    }

    @Test
    void testBuildEmbeddingStoreWithCreateStoreFalse() {
        try (MockedStatic<PineconeEmbeddingStore> pineconeStatic = mockStatic(PineconeEmbeddingStore.class)) {
            pineconeStatic.when(PineconeEmbeddingStore::builder).thenReturn(builder);
            when(builder.apiKey(anyString())).thenReturn(builder);
            when(builder.index(anyString())).thenReturn(builder);
            when(builder.nameSpace(anyString())).thenReturn(builder);
            when(builder.build()).thenReturn(embeddingStore);

            PineconeStore store = new PineconeStore(storeConfiguration, pineconeStoreConnection, STORE_NAME, queryParameters, DIMENSION, NO_CREATE_STORE);
            EmbeddingStore<?> result = store.buildEmbeddingStore();
            assertNotNull(result);
            assertEquals(embeddingStore, result);
        }
    }
} 
