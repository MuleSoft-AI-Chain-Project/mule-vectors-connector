package org.mule.extension.vectors.internal.store.qdrant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.qdrant.QdrantStore;
import org.mule.extension.vectors.internal.service.store.qdrant.QdrantStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.List;

import com.google.common.util.concurrent.Futures;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QdrantStoreTest {

  @Mock
  StoreConfiguration storeConfiguration;
  @Mock
  QdrantStoreConnection qdrantStoreConnection;
  @Mock
  QdrantClient qdrantClient;
  @Mock
  QueryParameters queryParameters;
  @Mock
  QdrantEmbeddingStore.Builder embeddingStoreBuilder;
  @Mock
  QdrantEmbeddingStore embeddingStore;

  @BeforeEach
  void setUp() {
    lenient().when(qdrantStoreConnection.getClient()).thenReturn(qdrantClient);
    lenient().when(qdrantStoreConnection.getTextSegmentKey()).thenReturn("text");
    lenient().when(queryParameters.pageSize()).thenReturn(2);
    lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
  }

  private QdrantStore createStore(String name, int dim, boolean create) throws Exception {
    when(qdrantClient.collectionExistsAsync(anyString())).thenReturn(Futures.immediateFuture(true));
    return new QdrantStore(storeConfiguration, qdrantStoreConnection, name, queryParameters, dim, create);
  }

  @Test
  void constructor_createsCollectionIfNotExists() throws Exception {
    when(qdrantClient.collectionExistsAsync(anyString())).thenReturn(Futures.immediateFuture(false));
    doNothing().when(qdrantStoreConnection).createCollection(anyString(), anyInt());
    new QdrantStore(storeConfiguration, qdrantStoreConnection, "testStore", queryParameters, 128, true);
    verify(qdrantStoreConnection).createCollection(eq("testStore"), eq(128));
  }

  @Test
  void constructor_doesNotCreateCollectionIfExists() throws Exception {
    when(qdrantClient.collectionExistsAsync(anyString())).thenReturn(Futures.immediateFuture(true));
    new QdrantStore(storeConfiguration, qdrantStoreConnection, "testStore", queryParameters, 128, true);
    verify(qdrantStoreConnection, never()).createCollection(anyString(), anyInt());
  }

  @Test
  void constructor_doesNotCreateWhenCreateStoreIsFalse() throws Exception {
    QdrantStore store = new QdrantStore(storeConfiguration, qdrantStoreConnection, "testStore", queryParameters, 128, false);
    verify(qdrantClient, never()).collectionExistsAsync(anyString());
    assertThat(store).isNotNull();
  }

  @Test
  void constructor_doesNotCreateWhenDimensionIsZero() throws Exception {
    when(qdrantClient.collectionExistsAsync(anyString())).thenReturn(Futures.immediateFuture(false));
    new QdrantStore(storeConfiguration, qdrantStoreConnection, "testStore", queryParameters, 0, true);
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
  void constructor_throwsModuleExceptionOnInterruptedException() throws Exception {
    when(qdrantClient.collectionExistsAsync(anyString()))
        .thenReturn(Futures.immediateFailedFuture(new InterruptedException("interrupted")));
    assertThatThrownBy(() -> new QdrantStore(storeConfiguration, qdrantStoreConnection, "testStore", queryParameters, 128, true))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Qdrant API request failed");
  }

  @Test
  void buildEmbeddingStore_returnsQdrantEmbeddingStore() throws Exception {
    try (var builderMocked = mockStatic(QdrantEmbeddingStore.class)) {
      builderMocked.when(QdrantEmbeddingStore::builder).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.client(qdrantClient)).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.payloadTextKey("text")).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.collectionName("testStore")).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.build()).thenReturn(embeddingStore);
      QdrantStore store = createStore("testStore", 128, true);
      EmbeddingStore<TextSegment> result = store.buildEmbeddingStore();
      assertThat(result).isSameAs(embeddingStore);
    }
  }

  @Test
  void buildEmbeddingStore_throwsModuleExceptionOnError() throws Exception {
    try (var builderMocked = mockStatic(QdrantEmbeddingStore.class)) {
      builderMocked.when(QdrantEmbeddingStore::builder).thenThrow(new RuntimeException("fail"));
      QdrantStore store = createStore("testStore", 128, true);
      assertThatThrownBy(store::buildEmbeddingStore)
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Failed to build Qdrant embedding store");
    }
  }

  @Test
  void query_delegatesToSuperOnSuccess() throws Exception {
    try (var builderMocked = mockStatic(QdrantEmbeddingStore.class)) {
      builderMocked.when(QdrantEmbeddingStore::builder).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.client(any())).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.payloadTextKey(any())).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.collectionName(any())).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.build()).thenReturn(embeddingStore);

      TextSegment segment = new TextSegment("query text", new dev.langchain4j.data.document.Metadata());
      Embedding queryEmbedding = new Embedding(new float[] {0.1f, 0.2f, 0.3f});
      EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.95, "id1", queryEmbedding, segment);
      EmbeddingSearchResult<TextSegment> searchResult = new EmbeddingSearchResult<>(List.of(match));
      when(embeddingStore.search(any(EmbeddingSearchRequest.class))).thenReturn(searchResult);

      QdrantStore store = createStore("testStore", 3, true);
      var result = store.query(List.of(segment), List.of(queryEmbedding), 5, 0.5, null);
      assertThat(result).isNotNull();
      assertThat(result.has("sources")).isTrue();
    }
  }

  @Test
  void query_rethrowsExceptionWithoutExtraNetworkCall() throws Exception {
    try (var builderMocked = mockStatic(QdrantEmbeddingStore.class)) {
      builderMocked.when(QdrantEmbeddingStore::builder).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.client(any())).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.payloadTextKey(any())).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.collectionName(any())).thenReturn(embeddingStoreBuilder);
      when(embeddingStoreBuilder.build()).thenReturn(embeddingStore);
      when(embeddingStore.search(any(EmbeddingSearchRequest.class)))
          .thenThrow(new RuntimeException("query failed"));

      QdrantStore store = createStore("testStore", 3, true);
      Embedding qEmb = new Embedding(new float[] {0.1f, 0.2f, 0.3f});
      TextSegment seg = new TextSegment("text", new dev.langchain4j.data.document.Metadata());
      assertThatThrownBy(() -> store.query(List.of(seg), List.of(qEmb), 5, 0.5, null))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("query failed");
      verifyNoMoreInteractions(qdrantClient);
    }
  }

  @Test
  void getFileIterator_returnsQdrantStoreIterator() throws Exception {
    QdrantStore store = createStore("testStore", 128, true);
    QdrantStoreIterator<?> iterator = store.getFileIterator();
    assertThat(iterator).isNotNull().isInstanceOf(QdrantStoreIterator.class);
  }
}
