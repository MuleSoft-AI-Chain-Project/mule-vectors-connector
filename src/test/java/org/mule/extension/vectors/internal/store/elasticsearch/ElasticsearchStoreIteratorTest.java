package org.mule.extension.vectors.internal.store.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.elasticsearch.ElasticsearchStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElasticsearchStoreIteratorTest {
    @Mock ElasticsearchStoreConnection connection;
    @Mock RestClient restClient;
    @Mock QueryParameters queryParameters;
    @Mock ElasticsearchClient client;
    @Mock SearchResponse<Map> searchResponse;
    @Mock Hit<Map> hit;
    @Mock co.elastic.clients.elasticsearch.core.search.HitsMetadata<Map> hitsMetadata;

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
}
