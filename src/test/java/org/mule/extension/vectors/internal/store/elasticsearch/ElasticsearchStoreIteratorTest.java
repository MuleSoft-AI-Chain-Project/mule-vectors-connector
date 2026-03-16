package org.mule.extension.vectors.internal.store.elasticsearch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.elasticsearch.ElasticsearchStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.util.*;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.ClearScrollRequest;
import co.elastic.clients.elasticsearch.core.ScrollRequest;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchStoreIteratorTest {

  @Mock
  ElasticsearchStoreConnection connection;
  @Mock
  RestClient restClient;
  @Mock
  QueryParameters queryParameters;
  @Mock
  ElasticsearchClient client;
  @Mock
  SearchResponse<Map> searchResponse;
  @Mock
  Hit<Map> hit;
  @Mock
  co.elastic.clients.elasticsearch.core.search.HitsMetadata<Map> hitsMetadata;

  @BeforeEach
  void setUp() {
    lenient().when(connection.getRestClient()).thenReturn(restClient);
    lenient().when(queryParameters.pageSize()).thenReturn(2);
    lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
  }

  @Test
  void next_returnsVectorStoreRowWithCorrectFields() throws Exception {
    // Mock the client and response chain
    try (MockedConstruction<ElasticsearchClient> clientConstruction =
        Mockito.mockConstruction(ElasticsearchClient.class, (mock, context) -> {
          when(mock.search(any(SearchRequest.class), eq(Map.class))).thenReturn(searchResponse);
        })) {
      Map<String, Object> sourceMap = new HashMap<>();
      sourceMap.put("vector", Arrays.asList(0.1, 0.2, 0.3));
      sourceMap.put("text", "doc1");
      sourceMap.put("metadata", Map.of("foo", "bar"));
      when(hit.id()).thenReturn("id1");
      when(hit.source()).thenReturn(sourceMap);
      when(hitsMetadata.hits()).thenReturn(List.of(hit)).thenReturn(Collections.emptyList());
      when(searchResponse.hits()).thenReturn(hitsMetadata);
      when(searchResponse.scrollId()).thenReturn(null);

      ElasticsearchStoreIterator<?> iterator = new ElasticsearchStoreIterator<>(connection, "store", queryParameters);
      assertThat(iterator.hasNext()).isTrue();
      var row = iterator.next();
      assertThat(row.getId()).isEqualTo("id1");
      assertThat(row.getEmbedded()).isNotNull();
      assertThat(row.getEmbedding()).isNotNull();
      assertThat(row.getEmbedding().vector()).containsExactly(0.1f, 0.2f, 0.3f);
      assertThat(iterator.hasNext()).isFalse();
    }
  }

  @Test
  void next_whenNoMoreElements_throwsNoSuchElementException() throws Exception {
    try (MockedConstruction<ElasticsearchClient> clientConstruction =
        Mockito.mockConstruction(ElasticsearchClient.class, (mock, context) -> {
          when(mock.search(any(SearchRequest.class), eq(Map.class))).thenReturn(searchResponse);
        })) {
      when(hitsMetadata.hits()).thenReturn(Collections.emptyList());
      when(searchResponse.hits()).thenReturn(hitsMetadata);
      when(searchResponse.scrollId()).thenReturn(null);
      ElasticsearchStoreIterator<?> iterator = new ElasticsearchStoreIterator<>(connection, "store", queryParameters);
      assertThat(iterator.hasNext()).isFalse();
      assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
    }
  }

  @Test
  void constructor_whenIOException_throwsModuleException() throws Exception {
    try (MockedConstruction<ElasticsearchClient> clientConstruction =
        Mockito.mockConstruction(ElasticsearchClient.class, (mock, context) -> {
          when(mock.search(any(SearchRequest.class), eq(Map.class))).thenThrow(new IOException("fail"));
        })) {
      assertThatThrownBy(() -> new ElasticsearchStoreIterator<>(connection, "store", queryParameters))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Error");
    }
  }

  @Test
  void next_withoutEmbeddings_returnsRowWithNullEmbedding() throws Exception {
    when(queryParameters.retrieveEmbeddings()).thenReturn(false);
    try (MockedConstruction<ElasticsearchClient> clientConstruction =
        Mockito.mockConstruction(ElasticsearchClient.class, (mock, context) -> {
          when(mock.search(any(SearchRequest.class), eq(Map.class))).thenReturn(searchResponse);
        })) {
      Map<String, Object> sourceMap = new HashMap<>();
      sourceMap.put("vector", Arrays.asList(0.1, 0.2, 0.3));
      sourceMap.put("text", "doc1");
      sourceMap.put("metadata", Map.of("foo", "bar"));
      when(hit.id()).thenReturn("id1");
      when(hit.source()).thenReturn(sourceMap);
      when(hitsMetadata.hits()).thenReturn(List.of(hit)).thenReturn(Collections.emptyList());
      when(searchResponse.hits()).thenReturn(hitsMetadata);
      when(searchResponse.scrollId()).thenReturn(null);

      ElasticsearchStoreIterator<?> iterator = new ElasticsearchStoreIterator<>(connection, "store", queryParameters);
      var row = iterator.next();
      assertThat(row.getEmbedding()).isNull();
      assertThat(row.getEmbedded()).isNotNull();
    }
  }

  @Test
  void close_clearsScrollContext() throws Exception {
    try (MockedConstruction<ElasticsearchClient> clientConstruction =
        Mockito.mockConstruction(ElasticsearchClient.class, (mock, context) -> {
          when(mock.search(any(SearchRequest.class), eq(Map.class))).thenReturn(searchResponse);
          when(mock.clearScroll(any(ClearScrollRequest.class))).thenReturn(null);
        })) {
      when(hitsMetadata.hits()).thenReturn(List.of(hit));
      when(searchResponse.hits()).thenReturn(hitsMetadata);
      when(searchResponse.scrollId()).thenReturn("scroll-123");

      ElasticsearchStoreIterator<?> iterator = new ElasticsearchStoreIterator<>(connection, "store", queryParameters);
      iterator.close();

      ElasticsearchClient constructedClient = clientConstruction.constructed().get(0);
      verify(constructedClient).clearScroll(any(ClearScrollRequest.class));
    }
  }

  @Test
  void close_handlesIOExceptionGracefully() throws Exception {
    try (MockedConstruction<ElasticsearchClient> clientConstruction =
        Mockito.mockConstruction(ElasticsearchClient.class, (mock, context) -> {
          when(mock.search(any(SearchRequest.class), eq(Map.class))).thenReturn(searchResponse);
          when(mock.clearScroll(any(ClearScrollRequest.class))).thenThrow(new IOException("clear scroll failed"));
        })) {
      when(hitsMetadata.hits()).thenReturn(List.of(hit));
      when(searchResponse.hits()).thenReturn(hitsMetadata);
      when(searchResponse.scrollId()).thenReturn("scroll-123");

      ElasticsearchStoreIterator<?> iterator = new ElasticsearchStoreIterator<>(connection, "store", queryParameters);
      assertThatCode(iterator::close).doesNotThrowAnyException();
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void fetchNextBatch_usesScrollWhenScrollIdPresent() throws Exception {
    Hit<Map> hit2 = mock(Hit.class);
    ScrollResponse<Map> scrollResponse = mock(ScrollResponse.class);
    co.elastic.clients.elasticsearch.core.search.HitsMetadata<Map> scrollHits =
        mock(co.elastic.clients.elasticsearch.core.search.HitsMetadata.class);

    try (MockedConstruction<ElasticsearchClient> clientConstruction =
        Mockito.mockConstruction(ElasticsearchClient.class, (mock, context) -> {
          when(mock.search(any(SearchRequest.class), eq(Map.class))).thenReturn(searchResponse);
          when(mock.scroll(any(ScrollRequest.class), eq(Map.class))).thenReturn(scrollResponse);
        })) {
      Map<String, Object> sourceMap1 = new HashMap<>();
      sourceMap1.put("vector", Arrays.asList(0.1, 0.2));
      sourceMap1.put("text", "doc1");
      sourceMap1.put("metadata", Map.of());
      when(hit.id()).thenReturn("id1");
      when(hit.source()).thenReturn(sourceMap1);

      Map<String, Object> sourceMap2 = new HashMap<>();
      sourceMap2.put("vector", Arrays.asList(0.3, 0.4));
      sourceMap2.put("text", "doc2");
      sourceMap2.put("metadata", Map.of());
      when(hit2.id()).thenReturn("id2");
      when(hit2.source()).thenReturn(sourceMap2);

      when(hitsMetadata.hits()).thenReturn(List.of(hit));
      when(searchResponse.hits()).thenReturn(hitsMetadata);
      when(searchResponse.scrollId()).thenReturn("scroll-123");

      when(scrollHits.hits()).thenReturn(List.of(hit2));
      when(scrollResponse.hits()).thenReturn(scrollHits);
      when(scrollResponse.scrollId()).thenReturn(null);

      ElasticsearchStoreIterator<?> iterator = new ElasticsearchStoreIterator<>(connection, "store", queryParameters);
      assertThat(iterator.hasNext()).isTrue();
      var row1 = iterator.next();
      assertThat(row1.getId()).isEqualTo("id1");

      assertThat(iterator.hasNext()).isTrue();
      var row2 = iterator.next();
      assertThat(row2.getId()).isEqualTo("id2");

      ElasticsearchClient constructedClient = clientConstruction.constructed().get(0);
      verify(constructedClient).scroll(any(ScrollRequest.class), eq(Map.class));
    }
  }
}
