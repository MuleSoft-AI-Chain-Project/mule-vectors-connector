package org.mule.extension.vectors.internal.store.chroma;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.store.chroma.ChromaStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChromaStoreIteratorTest {
    @Mock ChromaStoreConnection chromaStoreConnection;
    @Mock QueryParameters queryParameters;
    @Mock HttpClient httpClient;
    @Mock HttpResponse httpResponse;

    @BeforeEach
    void setUp() {
        lenient().when(chromaStoreConnection.getHttpClient()).thenReturn(httpClient);
        lenient().when(chromaStoreConnection.getUrl()).thenReturn("http://chroma");
        lenient().when(queryParameters.pageSize()).thenReturn(2);
        lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
    }

    @Test
    void constructor_loadsFirstPageAndFields() throws Exception {
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            CompletableFuture<HttpResponse> future1 = CompletableFuture.completedFuture(httpResponse);
            CompletableFuture<HttpResponse> future2 = CompletableFuture.completedFuture(httpResponse);
            CompletableFuture<HttpResponse> future3 = CompletableFuture.completedFuture(httpResponse);
            helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())).thenReturn(future1, future2, future3);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt())).thenReturn(future3);
            when(httpResponse.getStatusCode()).thenReturn(200);
            HttpEntity entity = mock(HttpEntity.class);
            when(httpResponse.getEntity()).thenReturn(entity);
            when(entity.getContent()).thenReturn(new ByteArrayInputStream("{\"id\":\"col123\"}".getBytes()))
                .thenReturn(new ByteArrayInputStream("1".getBytes()))
                .thenReturn(new ByteArrayInputStream(("{" +
                        "\"ids\":[\"id1\"]," +
                        "\"metadatas\":[{\"foo\":\"bar\"}]," +
                        "\"documents\":[\"doc1\"]," +
                        "\"embeddings\":[[0.1,0.2,0.3]]}").getBytes()));
            ChromaStoreIterator<TextSegment>
                iterator = new ChromaStoreIterator<>(chromaStoreConnection, "testStore", queryParameters);
            var idsField = iterator.getClass().getDeclaredField("ids");
            idsField.setAccessible(true);
            assertThat((java.util.List<String>) idsField.get(iterator)).containsExactly("id1");
        }
    }

    @Test
    void next_returnsVectorStoreRowWithCorrectFields() throws Exception {
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            CompletableFuture<HttpResponse> future1 = CompletableFuture.completedFuture(httpResponse);
            CompletableFuture<HttpResponse> future2 = CompletableFuture.completedFuture(httpResponse);
            CompletableFuture<HttpResponse> future3 = CompletableFuture.completedFuture(httpResponse);
            helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())).thenReturn(future1, future2, future3);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt())).thenReturn(future3);
            when(httpResponse.getStatusCode()).thenReturn(200);
            HttpEntity entity = mock(HttpEntity.class);
            when(httpResponse.getEntity()).thenReturn(entity);
            when(entity.getContent()).thenReturn(
                new ByteArrayInputStream("{\"id\":\"col123\"}".getBytes()),
                new ByteArrayInputStream("1".getBytes()),
                new ByteArrayInputStream(("{" +
                        "\"ids\":[\"id1\"]," +
                        "\"metadatas\":[{\"foo\":\"bar\"}]," +
                        "\"documents\":[\"doc1\"]," +
                        "\"embeddings\":[[0.1,0.2,0.3]]}").getBytes())
            );
            ChromaStoreIterator<TextSegment> iterator = new ChromaStoreIterator<>(chromaStoreConnection, "testStore", queryParameters);
            assertThat(iterator.hasNext()).isTrue();
            var row = iterator.next();
            assertThat(row.getId()).isEqualTo("id1");
            assertThat(row.getEmbedded()).isInstanceOf(TextSegment.class);
            assertThat(((TextSegment) row.getEmbedded()).text()).isEqualTo("doc1");
            assertThat(row.getEmbedding()).isInstanceOf(Embedding.class);
            assertThat(((Embedding) row.getEmbedding()).vector()).containsExactly(0.1f, 0.2f, 0.3f);
        }
    }

    @Test
    void next_whenNoMoreElements_throwsNoSuchElementException() throws Exception {
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            CompletableFuture<HttpResponse> future1 = CompletableFuture.completedFuture(httpResponse);
            CompletableFuture<HttpResponse> future2 = CompletableFuture.completedFuture(httpResponse);
            CompletableFuture<HttpResponse> future3 = CompletableFuture.completedFuture(httpResponse);
            helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())).thenReturn(future1, future2, future3);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt())).thenReturn(future3);
            when(httpResponse.getStatusCode()).thenReturn(200);
            HttpEntity entity = mock(HttpEntity.class);
            when(httpResponse.getEntity()).thenReturn(entity);
            when(entity.getContent()).thenReturn(
                new ByteArrayInputStream("{\"id\":\"col123\"}".getBytes()),
                new ByteArrayInputStream("1".getBytes()),
                new ByteArrayInputStream(("{" +
                        "\"ids\":[\"id1\"]," +
                        "\"metadatas\":[{\"foo\":\"bar\"}]," +
                        "\"documents\":[\"doc1\"]," +
                        "\"embeddings\":[[0.1,0.2,0.3]]}").getBytes())
            );
            ChromaStoreIterator<TextSegment> iterator = new ChromaStoreIterator<>(chromaStoreConnection, "testStore", queryParameters);
            assertThat(iterator.hasNext()).isTrue();
            iterator.next();
            assertThat(iterator.hasNext()).isFalse();
            assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Test
    void constructor_whenIOException_throwsModuleException() throws Exception {
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(httpResponse);
            helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())).thenReturn(future);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt())).thenReturn(future);
            when(httpResponse.getStatusCode()).thenReturn(200);
            HttpEntity entity = mock(HttpEntity.class);
            when(httpResponse.getEntity()).thenReturn(entity);
            when(entity.getContent()).thenAnswer(invocation -> { throw new IOException("fail"); });
            assertThatThrownBy(() -> new ChromaStoreIterator<>(chromaStoreConnection, "testStore", queryParameters))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Error reading response from Chroma");
        }
    }
}
