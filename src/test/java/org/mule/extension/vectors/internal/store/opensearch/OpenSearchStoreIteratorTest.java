package org.mule.extension.vectors.internal.store.opensearch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.opensearch.OpenSearchStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.util.*;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.ScrollResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class OpenSearchStoreIteratorTest {

  @Mock
  OpenSearchStoreConnection connection;
  @Mock
  OpenSearchClient openSearchClient;
  @Mock
  QueryParameters queryParameters;

  @BeforeEach
  void setUp() {
    lenient().when(connection.getOpenSearchClient()).thenReturn(openSearchClient);
    lenient().when(queryParameters.pageSize()).thenReturn(2);
    lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
  }

  @Test
  void next_returnsVectorStoreRowWithCorrectFields() throws Exception {
    SearchResponse<Map<String, Object>> searchResponse = mock(SearchResponse.class);
    HitsMetadata<Map<String, Object>> hitsMetadata = mock(HitsMetadata.class);

    Hit<Map<String, Object>> hit = mock(Hit.class);
    Map<String, Object> sourceMap = new HashMap<>();
    sourceMap.put("vector", Arrays.asList(0.1, 0.2, 0.3));
    sourceMap.put("text", "doc1");
    sourceMap.put("metadata", Map.of("foo", "bar"));
    when(hit.id()).thenReturn("id1");
    when(hit.source()).thenReturn(sourceMap);

    when(hitsMetadata.hits()).thenReturn(List.of(hit));
    when(searchResponse.hits()).thenReturn(hitsMetadata);
    when(searchResponse.scrollId()).thenReturn(null);
    when(openSearchClient.search(any(SearchRequest.class), eq(Map.class))).thenReturn((SearchResponse) searchResponse);

    OpenSearchStoreIterator<?> iterator = new OpenSearchStoreIterator<>(connection, "testStore", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    var row = iterator.next();
    assertThat(row.getId()).isEqualTo("id1");
    assertThat(row.getEmbedded()).isInstanceOf(TextSegment.class);
    assertThat(((TextSegment) row.getEmbedded()).text()).isEqualTo("doc1");
    assertThat(row.getEmbedding()).isInstanceOf(Embedding.class);
    assertThat(row.getEmbedding().vector()).containsExactly(0.1f, 0.2f, 0.3f);
  }

  @Test
  void next_whenNoEmbeddings_returnsNullEmbedding() throws Exception {
    when(queryParameters.retrieveEmbeddings()).thenReturn(false);
    SearchResponse<Map<String, Object>> searchResponse = mock(SearchResponse.class);
    HitsMetadata<Map<String, Object>> hitsMetadata = mock(HitsMetadata.class);

    Hit<Map<String, Object>> hit = mock(Hit.class);
    Map<String, Object> sourceMap = new HashMap<>();
    sourceMap.put("text", "doc1");
    sourceMap.put("metadata", Map.of("foo", "bar"));
    when(hit.id()).thenReturn("id1");
    when(hit.source()).thenReturn(sourceMap);

    when(hitsMetadata.hits()).thenReturn(List.of(hit));
    when(searchResponse.hits()).thenReturn(hitsMetadata);
    when(searchResponse.scrollId()).thenReturn(null);
    when(openSearchClient.search(any(SearchRequest.class), eq(Map.class))).thenReturn((SearchResponse) searchResponse);

    OpenSearchStoreIterator<?> iterator = new OpenSearchStoreIterator<>(connection, "testStore", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    var row = iterator.next();
    assertThat(row.getEmbedding()).isNull();
  }

  @Test
  void next_whenNoMoreElements_throwsNoSuchElementException() throws Exception {
    SearchResponse<Map<String, Object>> searchResponse = mock(SearchResponse.class);
    HitsMetadata<Map<String, Object>> hitsMetadata = mock(HitsMetadata.class);
    when(hitsMetadata.hits()).thenReturn(Collections.emptyList());
    when(searchResponse.hits()).thenReturn(hitsMetadata);
    when(searchResponse.scrollId()).thenReturn(null);
    when(openSearchClient.search(any(SearchRequest.class), eq(Map.class))).thenReturn((SearchResponse) searchResponse);

    OpenSearchStoreIterator<?> iterator = new OpenSearchStoreIterator<>(connection, "testStore", queryParameters);
    assertThat(iterator.hasNext()).isFalse();
    assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void constructor_whenIOException_throwsModuleException() throws Exception {
    when(openSearchClient.search(any(SearchRequest.class), eq(Map.class))).thenThrow(new IOException("connection failed"));

    assertThatThrownBy(() -> new OpenSearchStoreIterator<>(connection, "testStore", queryParameters))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Store issue");
  }

  @Test
  void pagination_withScrollId() throws Exception {
    SearchResponse<Map<String, Object>> searchResponse = mock(SearchResponse.class);
    HitsMetadata<Map<String, Object>> hitsMetadata = mock(HitsMetadata.class);

    Hit<Map<String, Object>> hit1 = mock(Hit.class);
    Map<String, Object> src1 = new HashMap<>();
    src1.put("text", "doc1");
    src1.put("metadata", Map.of("k", "v"));
    src1.put("vector", Arrays.asList(0.1, 0.2));
    when(hit1.id()).thenReturn("id1");
    when(hit1.source()).thenReturn(src1);

    when(hitsMetadata.hits()).thenReturn(List.of(hit1));
    when(searchResponse.hits()).thenReturn(hitsMetadata);
    when(searchResponse.scrollId()).thenReturn("scroll-1");
    when(openSearchClient.search(any(SearchRequest.class), eq(Map.class))).thenReturn((SearchResponse) searchResponse);

    ScrollResponse<Map<String, Object>> scrollResponse = mock(ScrollResponse.class);
    HitsMetadata<Map<String, Object>> emptyHits = mock(HitsMetadata.class);
    when(emptyHits.hits()).thenReturn(Collections.emptyList());
    when(scrollResponse.hits()).thenReturn(emptyHits);
    when(scrollResponse.scrollId()).thenReturn("scroll-2");
    when(openSearchClient.scroll(any(ScrollRequest.class), eq(Map.class))).thenReturn((ScrollResponse) scrollResponse);

    OpenSearchStoreIterator<?> iterator = new OpenSearchStoreIterator<>(connection, "testStore", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    var row = iterator.next();
    assertThat(row.getId()).isEqualTo("id1");
    assertThat(iterator.hasNext()).isFalse();
  }

  @Test
  void hasNext_whenIOExceptionDuringScroll_throwsModuleException() throws Exception {
    SearchResponse<Map<String, Object>> searchResponse = mock(SearchResponse.class);
    HitsMetadata<Map<String, Object>> hitsMetadata = mock(HitsMetadata.class);

    Hit<Map<String, Object>> hit = mock(Hit.class);
    Map<String, Object> src = new HashMap<>();
    src.put("text", "doc1");
    src.put("metadata", Map.of("foo", "bar"));
    src.put("vector", Arrays.asList(0.1));
    when(hit.id()).thenReturn("id1");
    when(hit.source()).thenReturn(src);

    when(hitsMetadata.hits()).thenReturn(List.of(hit));
    when(searchResponse.hits()).thenReturn(hitsMetadata);
    when(searchResponse.scrollId()).thenReturn("scroll-1");
    when(openSearchClient.search(any(SearchRequest.class), eq(Map.class))).thenReturn((SearchResponse) searchResponse);

    when(openSearchClient.scroll(any(ScrollRequest.class), eq(Map.class))).thenThrow(new IOException("scroll failed"));

    OpenSearchStoreIterator<?> iterator = new OpenSearchStoreIterator<>(connection, "testStore", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    iterator.next();
    assertThatThrownBy(iterator::hasNext)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Error fetching next batch from OpenSearch");
  }
}
