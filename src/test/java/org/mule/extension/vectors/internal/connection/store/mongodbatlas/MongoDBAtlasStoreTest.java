package org.mule.extension.vectors.internal.connection.store.mongodbatlas;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.MongoSecurityException;
import com.mongodb.client.MongoClient;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.mongodbatlas.MongoDBAtlasStore;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoDBAtlasStoreTest {
    @Mock
    private StoreConfiguration storeConfiguration;
    @Mock
    private MongoDBAtlasStoreConnection mongoDBAtlasStoreConnection;
    @Mock
    private QueryParameters queryParameters;
    @Mock
    private MongoClient mongoClient;
    @Mock
    private MongoDbEmbeddingStore.Builder builder;
    @Mock
    private IndexMapping.Builder indexMappingBuilder;
    @Mock
    private IndexMapping indexMapping;
    @Mock
    private MongoDbEmbeddingStore embeddingStore;

    private static final String STORE_NAME = "test-mongo";
    private static final int DIMENSION = 128;
    private static final boolean CREATE_STORE = true;

    @BeforeEach
    void setUp() {
        lenient().when(mongoDBAtlasStoreConnection.getMongoClient()).thenReturn(mongoClient);
        lenient().when(mongoDBAtlasStoreConnection.getDatabase()).thenReturn("testdb");
    }

    @Test
    void testBuildEmbeddingStoreReturnsNonNull() {
        try (MockedStatic<MongoDbEmbeddingStore> staticMongo = mockStatic(MongoDbEmbeddingStore.class);
             MockedStatic<IndexMapping> staticIndex = mockStatic(IndexMapping.class)) {
            staticMongo.when(MongoDbEmbeddingStore::builder).thenReturn(builder);
            when(builder.databaseName(any())).thenReturn(builder);
            when(builder.collectionName(any())).thenReturn(builder);
            when(builder.createIndex(anyBoolean())).thenReturn(builder);
            when(builder.indexName(any())).thenReturn(builder);
            when(builder.fromClient(any())).thenReturn(builder);
            staticIndex.when(IndexMapping::builder).thenReturn(indexMappingBuilder);
            when(indexMappingBuilder.dimension(anyInt())).thenReturn(indexMappingBuilder);
            when(indexMappingBuilder.metadataFieldNames(any())).thenReturn(indexMappingBuilder);
            when(indexMappingBuilder.build()).thenReturn(indexMapping);
            when(builder.indexMapping(any())).thenReturn(builder);
            when(builder.build()).thenReturn(embeddingStore);

            MongoDBAtlasStore store = new MongoDBAtlasStore(
                    storeConfiguration,
                    mongoDBAtlasStoreConnection,
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
    void testBuildEmbeddingStoreThrowsModuleExceptionOnMongoSecurityException() {
        try (MockedStatic<MongoDbEmbeddingStore> staticMongo = mockStatic(MongoDbEmbeddingStore.class)) {
            staticMongo.when(MongoDbEmbeddingStore::builder).thenThrow(new MongoSecurityException(null, "fail"));
            MongoDBAtlasStore store = new MongoDBAtlasStore(
                    storeConfiguration,
                    mongoDBAtlasStoreConnection,
                    STORE_NAME,
                    queryParameters,
                    DIMENSION,
                    CREATE_STORE
            );
            assertThrows(ModuleException.class, store::buildEmbeddingStore);
        }
    }

    @Test
    void testBuildEmbeddingStoreThrowsModuleExceptionOnMongoCommandException() {
        try (MockedStatic<MongoDbEmbeddingStore> staticMongo = mockStatic(MongoDbEmbeddingStore.class)) {
            staticMongo.when(MongoDbEmbeddingStore::builder).thenThrow(mock(MongoCommandException.class));
            MongoDBAtlasStore store = new MongoDBAtlasStore(
                    storeConfiguration,
                    mongoDBAtlasStoreConnection,
                    STORE_NAME,
                    queryParameters,
                    DIMENSION,
                    CREATE_STORE
            );
            assertThrows(ModuleException.class, store::buildEmbeddingStore);
        }
    }

    @Test
    void testBuildEmbeddingStoreThrowsModuleExceptionOnMongoException() {
        try (MockedStatic<MongoDbEmbeddingStore> staticMongo = mockStatic(MongoDbEmbeddingStore.class)) {
            staticMongo.when(MongoDbEmbeddingStore::builder).thenThrow(new MongoException("fail"));
            MongoDBAtlasStore store = new MongoDBAtlasStore(
                    storeConfiguration,
                    mongoDBAtlasStoreConnection,
                    STORE_NAME,
                    queryParameters,
                    DIMENSION,
                    CREATE_STORE
            );
            assertThrows(ModuleException.class, store::buildEmbeddingStore);
        }
    }
} 
