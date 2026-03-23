package org.mule.extension.vectors.internal.service.store;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.api.helper.parameter.RemoveFilterParameters;
import org.mule.extension.vectors.api.helper.parameter.SearchFilterParameters;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;

import java.util.List;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaseStoreServiceTest {

  @Mock
  StoreConfiguration storeConfiguration;
  @Mock
  BaseStoreConnection storeConnection;
  @Mock
  EmbeddingStore<TextSegment> embeddingStore;

  TestableStoreService service;

  @BeforeEach
  void setUp() {
    service = new TestableStoreService(storeConfiguration, storeConnection, "testStore", 128, false, embeddingStore);
  }

  @Test
  void query_returnsJsonWithSourcesAndResponse() {
    Embedding queryEmbedding = Embedding.from(new float[] {0.1f, 0.2f});
    TextSegment seg = new TextSegment("hello", Metadata.from("key", "value"));

    EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.95, "emb-1", queryEmbedding, seg);
    EmbeddingSearchResult<TextSegment> searchResult = new EmbeddingSearchResult<>(List.of(match));
    when(embeddingStore.search(any(EmbeddingSearchRequest.class))).thenReturn(searchResult);

    List<TextSegment> segments = List.of(new TextSegment("question?", new Metadata()));
    List<Embedding> embeddings = List.of(queryEmbedding);

    JSONObject result = service.query(segments, embeddings, 5, 0.5, null);
    assertThat(result.getString("response")).contains("hello");
    assertThat(result.getString("storeName")).isEqualTo("testStore");
    assertThat(result.getInt("maxResults")).isEqualTo(5);
    assertThat(result.getDouble("minScore")).isEqualTo(0.5);
    assertThat(result.getJSONArray("sources").length()).isEqualTo(1);
    assertThat(result.getString("question")).isEqualTo("question?");
  }

  @Test
  void query_withFilter_appliesFilter() {
    Embedding queryEmbedding = Embedding.from(new float[] {0.1f});
    TextSegment seg = new TextSegment("filtered", Metadata.from("key", "val"));
    EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.9, "id-1", queryEmbedding, seg);
    EmbeddingSearchResult<TextSegment> searchResult = new EmbeddingSearchResult<>(List.of(match));
    when(embeddingStore.search(any(EmbeddingSearchRequest.class))).thenReturn(searchResult);
    when(storeConnection.getVectorStore()).thenReturn("CHROMA");

    SearchFilterParameters filterParams = mock(SearchFilterParameters.class);
    when(filterParams.isConditionSet()).thenReturn(true);
    Filter mockFilter = mock(Filter.class);
    when(filterParams.buildMetadataFilter()).thenReturn(mockFilter);

    JSONObject result = service.query(List.of(new TextSegment("q", new Metadata())),
                                      List.of(queryEmbedding), 5, 0.5, filterParams);
    assertThat(result.getJSONArray("sources").length()).isEqualTo(1);
  }

  @Test
  void query_withMultipleSegments_doesNotIncludeQuestion() {
    Embedding qEmb = Embedding.from(new float[] {0.1f});
    TextSegment seg = new TextSegment("answer", new Metadata());
    EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(0.9, "id", qEmb, seg);
    EmbeddingSearchResult<TextSegment> result = new EmbeddingSearchResult<>(List.of(match));
    when(embeddingStore.search(any(EmbeddingSearchRequest.class))).thenReturn(result);

    JSONObject json = service.query(List.of(new TextSegment("q1", new Metadata()), new TextSegment("q2", new Metadata())),
                                    List.of(qEmb), 3, 0.1, null);
    assertThat(json.has("question")).isFalse();
  }

  @Test
  void add_delegatesToEmbeddingStore() {
    Embedding emb = Embedding.from(new float[] {0.1f});
    TextSegment seg = new TextSegment("text", new Metadata());
    when(embeddingStore.addAll(anyList(), anyList())).thenReturn(List.of("id-1"));

    List<String> ids = service.add(List.of(emb), List.of(seg));
    assertThat(ids).containsExactly("id-1");
    verify(embeddingStore).addAll(List.of(emb), List.of(seg));
  }

  @Test
  void remove_byIds_delegatesToEmbeddingStore() {
    RemoveFilterParameters params = mock(RemoveFilterParameters.class);
    when(params.isIdsSet()).thenReturn(true);
    when(params.getIds()).thenReturn(List.of("id-1", "id-2"));

    service.remove(params);
    verify(embeddingStore).removeAll(List.of("id-1", "id-2"));
  }

  @Test
  void remove_byFilter_delegatesToEmbeddingStore() {
    RemoveFilterParameters params = mock(RemoveFilterParameters.class);
    when(params.isIdsSet()).thenReturn(false);
    when(params.isConditionSet()).thenReturn(true);
    Filter mockFilter = mock(Filter.class);
    when(params.buildMetadataFilter()).thenReturn(mockFilter);

    service.remove(params);
    verify(embeddingStore).removeAll(mockFilter);
  }

  @Test
  void remove_all_delegatesToEmbeddingStore() {
    RemoveFilterParameters params = mock(RemoveFilterParameters.class);
    when(params.isIdsSet()).thenReturn(false);
    when(params.isConditionSet()).thenReturn(false);

    service.remove(params);
    verify(embeddingStore).removeAll();
  }

  static class TestableStoreService extends BaseStoreService {

    private final EmbeddingStore<TextSegment> store;

    TestableStoreService(StoreConfiguration config, BaseStoreConnection conn, String storeName,
                         int dimension, boolean createStore, EmbeddingStore<TextSegment> store) {
      super(config, conn, storeName, dimension, createStore);
      this.store = store;
    }

    @Override
    public EmbeddingStore<TextSegment> buildEmbeddingStore() {
      return store;
    }

    @Override
    public VectoreStoreIterator<?> getFileIterator() {
      return null;
    }
  }
}
