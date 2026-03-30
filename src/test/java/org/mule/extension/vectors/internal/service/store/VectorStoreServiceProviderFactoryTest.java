package org.mule.extension.vectors.internal.service.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.pgvector.PGVectorStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.weaviate.WeaviateStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.aisearch.AISearchStore;
import org.mule.extension.vectors.internal.service.store.alloydb.AlloyDBStore;
import org.mule.extension.vectors.internal.service.store.chroma.ChromaStore;
import org.mule.extension.vectors.internal.service.store.elasticsearch.ElasticsearchStore;
import org.mule.extension.vectors.internal.service.store.ephemeralfile.EphemeralFileStore;
import org.mule.extension.vectors.internal.service.store.milvus.MilvusStore;
import org.mule.extension.vectors.internal.service.store.mongodbatlas.MongoDBAtlasStore;
import org.mule.extension.vectors.internal.service.store.opensearch.OpenSearchStore;
import org.mule.extension.vectors.internal.service.store.pgvector.PGVectorStore;
import org.mule.extension.vectors.internal.service.store.pinecone.PineconeStore;
import org.mule.extension.vectors.internal.service.store.qdrant.QdrantStore;
import org.mule.extension.vectors.internal.service.store.weaviate.WeaviateStore;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class VectorStoreServiceProviderFactoryTest {

  @ParameterizedTest
  @MethodSource("storeTypeProvider")
  void getService_returnsCorrectStoreService(String vectorStore,
                                             Class<? extends BaseStoreConnection> connectionClass,
                                             Class<?> storeClass)
      throws Exception {
    BaseStoreConnection conn = mock(connectionClass);
    when(conn.getVectorStore()).thenReturn(vectorStore);
    StoreConfiguration config = mock(StoreConfiguration.class);
    QueryParameters qp = mock(QueryParameters.class);

    try (MockedConstruction<?> ignored = Mockito.mockConstruction(storeClass)) {
      VectorStoreService service =
          VectorStoreServiceProviderFactory.getService(config, conn, "store", qp, 128, false);
      assertThat(service).isNotNull();
    }
  }

  static Stream<Arguments> storeTypeProvider() {
    return Stream.of(
                     Arguments.of("MILVUS", MilvusStoreConnection.class, MilvusStore.class),
                     Arguments.of("MONGODB_ATLAS", MongoDBAtlasStoreConnection.class, MongoDBAtlasStore.class),
                     Arguments.of("PGVECTOR", PGVectorStoreConnection.class, PGVectorStore.class),
                     Arguments.of("AI_SEARCH", AISearchStoreConnection.class, AISearchStore.class),
                     Arguments.of("CHROMA", ChromaStoreConnection.class, ChromaStore.class),
                     Arguments.of("PINECONE", PineconeStoreConnection.class, PineconeStore.class),
                     Arguments.of("ELASTICSEARCH", ElasticsearchStoreConnection.class, ElasticsearchStore.class),
                     Arguments.of("OPENSEARCH", OpenSearchStoreConnection.class, OpenSearchStore.class),
                     Arguments.of("QDRANT", QdrantStoreConnection.class, QdrantStore.class),
                     Arguments.of("ALLOYDB", AlloyDBStoreConnection.class, AlloyDBStore.class),
                     Arguments.of("EPHEMERAL_FILE", EphemeralFileStoreConnection.class, EphemeralFileStore.class),
                     Arguments.of("WEAVIATE", WeaviateStoreConnection.class, WeaviateStore.class));
  }

  @Test
  void getService_unsupportedStoreType_throwsModuleException() {
    BaseStoreConnection conn = mock(BaseStoreConnection.class);
    when(conn.getVectorStore()).thenReturn("UNSUPPORTED");
    StoreConfiguration config = mock(StoreConfiguration.class);
    QueryParameters qp = mock(QueryParameters.class);

    assertThatThrownBy(() -> VectorStoreServiceProviderFactory.getService(config, conn, "store", qp, 128, false))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("UNSUPPORTED");
  }
}
