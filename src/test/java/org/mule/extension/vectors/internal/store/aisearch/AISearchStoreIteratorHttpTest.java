package org.mule.extension.vectors.internal.store.aisearch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.store.aisearch.AISearchStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.ByteArrayInputStream;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AISearchStoreIteratorHttpTest {

  @Mock
  AISearchStoreConnection connection;
  @Mock
  QueryParameters queryParameters;
  @Mock
  HttpClient httpClient;
  @Mock
  HttpResponse httpResponse;

  @BeforeEach
  void setUp() {
    lenient().when(connection.getHttpClient()).thenReturn(httpClient);
    lenient().when(connection.getUrl()).thenReturn("https://search.example.com");
    lenient().when(connection.getApiKey()).thenReturn("test-api-key");
    lenient().when(queryParameters.pageSize()).thenReturn(2);
    lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
  }

  @Test
  void next_returnsRowWithEmbeddingsAndMetadata() throws Exception {
    String responseBody = "{\"value\":[{\"id\":\"id1\",\"content\":\"hello world\","
        + "\"content_vector\":[0.1,0.2,0.3],"
        + "\"metadata\":{\"attributes\":[{\"key\":\"source\",\"value\":\"test\"}]}}]}";

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));
      when(httpResponse.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(entity);
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));

      AISearchStoreIterator<TextSegment> iterator =
          new AISearchStoreIterator<>("testStore", queryParameters, connection);
      assertThat(iterator.hasNext()).isTrue();
      var row = iterator.next();
      assertThat(row.getId()).isEqualTo("id1");
      assertThat(row.getEmbedded()).isInstanceOf(TextSegment.class);
      assertThat(((TextSegment) row.getEmbedded()).text()).isEqualTo("hello world");
      assertThat(row.getEmbedding()).isInstanceOf(Embedding.class);
      assertThat(row.getEmbedding().vector()).containsExactly(0.1f, 0.2f, 0.3f);
    }
  }

  @Test
  void next_withoutEmbeddings_returnsNullEmbedding() throws Exception {
    when(queryParameters.retrieveEmbeddings()).thenReturn(false);
    String responseBody = "{\"value\":[{\"id\":\"id1\",\"content\":\"hello\","
        + "\"metadata\":{\"attributes\":[{\"key\":\"k\",\"value\":\"v\"}]}}]}";

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));
      when(httpResponse.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(entity);
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));

      AISearchStoreIterator<TextSegment> iterator =
          new AISearchStoreIterator<>("testStore", queryParameters, connection);
      var row = iterator.next();
      assertThat(row.getEmbedding()).isNull();
    }
  }

  @Test
  void next_withNoMetadata_logsWarning() throws Exception {
    String responseBody = "{\"value\":[{\"id\":\"id1\",\"content\":\"hello\","
        + "\"content_vector\":[0.1]}]}";

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));
      when(httpResponse.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(entity);
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));

      AISearchStoreIterator<TextSegment> iterator =
          new AISearchStoreIterator<>("testStore", queryParameters, connection);
      var row = iterator.next();
      assertThat(row.getId()).isEqualTo("id1");
    }
  }

  @Test
  void pagination_followsNextLink() throws Exception {
    String page1 = "{\"value\":[{\"id\":\"id1\",\"content\":\"page1\","
        + "\"content_vector\":[0.1],\"metadata\":{\"attributes\":[]}}],"
        + "\"@odata.nextLink\":\"https://search.example.com/next\"}";
    String page2 = "{\"value\":[{\"id\":\"id2\",\"content\":\"page2\","
        + "\"content_vector\":[0.2],\"metadata\":{\"attributes\":[]}}]}";

    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpEntity entity1 = mock(HttpEntity.class);
      when(entity1.getContent()).thenReturn(new ByteArrayInputStream(page1.getBytes()));
      HttpResponse response1 = mock(HttpResponse.class);
      when(response1.getStatusCode()).thenReturn(200);
      when(response1.getEntity()).thenReturn(entity1);

      HttpEntity entity2 = mock(HttpEntity.class);
      when(entity2.getContent()).thenReturn(new ByteArrayInputStream(page2.getBytes()));
      HttpResponse response2 = mock(HttpResponse.class);
      when(response2.getStatusCode()).thenReturn(200);
      when(response2.getEntity()).thenReturn(entity2);

      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(response1))
          .thenReturn(CompletableFuture.completedFuture(response2));

      AISearchStoreIterator<TextSegment> iterator =
          new AISearchStoreIterator<>("testStore", queryParameters, connection);
      assertThat(iterator.hasNext()).isTrue();
      assertThat(iterator.next().getId()).isEqualTo("id1");
      assertThat(iterator.hasNext()).isTrue();
      assertThat(iterator.next().getId()).isEqualTo("id2");
      assertThat(iterator.hasNext()).isFalse();
    }
  }

  @Test
  void fetchNextBatch_handles401Error() throws Exception {
    String errorBody = "Unauthorized";
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent()).thenReturn(new ByteArrayInputStream(errorBody.getBytes()));
      when(httpResponse.getStatusCode()).thenReturn(401);
      when(httpResponse.getEntity()).thenReturn(entity);
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));

      assertThatThrownBy(() -> new AISearchStoreIterator<>("testStore", queryParameters, connection))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Authentication failed");
    }
  }

  @Test
  void fetchNextBatch_handles400Error() throws Exception {
    String errorBody = "Bad Request";
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent()).thenReturn(new ByteArrayInputStream(errorBody.getBytes()));
      when(httpResponse.getStatusCode()).thenReturn(400);
      when(httpResponse.getEntity()).thenReturn(entity);
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));

      assertThatThrownBy(() -> new AISearchStoreIterator<>("testStore", queryParameters, connection))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("Invalid request");
    }
  }

  @Test
  void fetchNextBatch_handles500Error() throws Exception {
    String errorBody = "Internal Error";
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent()).thenReturn(new ByteArrayInputStream(errorBody.getBytes()));
      when(httpResponse.getStatusCode()).thenReturn(500);
      when(httpResponse.getEntity()).thenReturn(entity);
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));

      assertThatThrownBy(() -> new AISearchStoreIterator<>("testStore", queryParameters, connection))
          .isInstanceOf(ModuleException.class)
          .hasMessageContaining("service error");
    }
  }

  @Test
  void next_throwsNoSuchElementWhenEmpty() throws Exception {
    String responseBody = "{\"value\":[]}";
    try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
      HttpEntity entity = mock(HttpEntity.class);
      when(entity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes()));
      when(httpResponse.getStatusCode()).thenReturn(200);
      when(httpResponse.getEntity()).thenReturn(entity);
      helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
          .thenReturn(CompletableFuture.completedFuture(httpResponse));

      AISearchStoreIterator<TextSegment> iterator =
          new AISearchStoreIterator<>("testStore", queryParameters, connection);
      assertThat(iterator.hasNext()).isFalse();
      assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
    }
  }
}
