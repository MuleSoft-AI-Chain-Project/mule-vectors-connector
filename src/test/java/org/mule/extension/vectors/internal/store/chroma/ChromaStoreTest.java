package org.mule.extension.vectors.internal.store.chroma;

import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.store.chroma.ChromaStore;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.HttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChromaStoreTest {
    @Mock StoreConfiguration storeConfiguration;
    @Mock ChromaStoreConnection chromaStoreConnection;
    @Mock QueryParameters queryParameters;
    @Mock HttpClient httpClient;
    @Mock HttpResponse httpResponse;

    ChromaStore chromaStore;

    @BeforeEach
    void setUp() {
        chromaStore = new ChromaStore(storeConfiguration, chromaStoreConnection, "testStore", queryParameters, 128, true);
    }

    @Test
    void constructor_assignsFields() throws Exception {
        assertThat(chromaStore).isNotNull();
        // Use reflection for private fields
        var queryParamsField = chromaStore.getClass().getDeclaredField("queryParams");
        queryParamsField.setAccessible(true);
        assertThat(queryParamsField.get(chromaStore)).isEqualTo(queryParameters);
        var connField = chromaStore.getClass().getDeclaredField("chromaStoreConnection");
        connField.setAccessible(true);
        assertThat(connField.get(chromaStore)).isEqualTo(chromaStoreConnection);
    }

    @Test
    void buildEmbeddingStore_returnsChromaEmbeddingStore() {
        try (MockedStatic<ChromaEmbeddingStore> staticMock = mockStatic(ChromaEmbeddingStore.class)) {
            ChromaEmbeddingStore.Builder builder = mock(ChromaEmbeddingStore.Builder.class, RETURNS_SELF);
            ChromaEmbeddingStore store = mock(ChromaEmbeddingStore.class);
            staticMock.when(ChromaEmbeddingStore::builder).thenReturn(builder);
            when(builder.baseUrl(any())).thenReturn(builder);
            when(builder.collectionName(any())).thenReturn(builder);
            when(builder.build()).thenReturn(store);
            EmbeddingStore<?> result = chromaStore.buildEmbeddingStore();
            assertThat(result).isSameAs(store);
            verify(builder).baseUrl(any());
            verify(builder).collectionName(any());
            verify(builder).build();
        }
    }

    @Test
    void getJsonResponse_success_returnsBody() throws Exception {
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(httpResponse);
            helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())).thenReturn(future);
            when(httpResponse.getStatusCode()).thenReturn(200);
            HttpEntity entity = mock(HttpEntity.class);
            when(entity.getContent()).thenReturn(new ByteArrayInputStream("{\"foo\":\"bar\"}".getBytes()));
            when(httpResponse.getEntity()).thenReturn(entity);
            var method = chromaStore.getClass().getDeclaredMethod("getJsonResponse", String.class, String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(chromaStore, "/api/v1/collections/test", null);
            assertThat(result).contains("foo");
        }
    }

    @Test
    void getJsonResponse_non200_throwsModuleException() throws Exception {
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(httpResponse);
            helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())).thenReturn(future);
            when(httpResponse.getStatusCode()).thenReturn(500);
            HttpEntity entity = mock(HttpEntity.class);
            when(entity.getContent()).thenReturn(new ByteArrayInputStream("fail".getBytes()));
            when(httpResponse.getEntity()).thenReturn(entity);
            var method = chromaStore.getClass().getDeclaredMethod("getJsonResponse", String.class, String.class);
            method.setAccessible(true);
            assertThatThrownBy(() -> method.invoke(chromaStore, "/fail", null))
                    .hasRootCauseInstanceOf(ModuleException.class)
                    .extracting(Throwable::getCause)
                    .satisfies(cause -> assertThat(((Throwable) cause).getMessage()).contains("Chroma API request failed"));
        }
    }

    @Test
    void getJsonResponse_ioException_throwsModuleException() throws Exception {
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(httpResponse);
            helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())).thenReturn(future);
            when(httpResponse.getStatusCode()).thenReturn(200);
            HttpEntity entity = mock(HttpEntity.class);
            when(httpResponse.getEntity()).thenReturn(entity);
            when(entity.getContent()).thenAnswer(invocation -> { throw new IOException("fail"); });
            assertThat(httpResponse.getEntity()).isSameAs(entity);
            var method = chromaStore.getClass().getDeclaredMethod("getJsonResponse", String.class, String.class);
            method.setAccessible(true);
            Throwable thrown = catchThrowable(() -> method.invoke(chromaStore, "/fail", null));
            // Traverse the cause chain to check for ModuleException
            boolean found = false;
            Throwable t = thrown;
            while (t != null) {
                if (t instanceof ModuleException) {
                    found = true;
                    break;
                }
                t = t.getCause();
            }
            assertThat(found).as("Exception chain contains ModuleException").isTrue();
        }
    }

    @Test
    void getJsonResponse_interruptedOrExecutionException_throwsModuleException() throws Exception {
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            CompletableFuture<HttpResponse> future = new CompletableFuture<>();
            future.completeExceptionally(new InterruptedException("fail"));
            helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())).thenReturn(future);
            var method = chromaStore.getClass().getDeclaredMethod("getJsonResponse", String.class, String.class);
            method.setAccessible(true);
            assertThatThrownBy(() -> method.invoke(chromaStore, "/fail", null))
                    .hasRootCauseInstanceOf(InterruptedException.class)
                    .extracting(Throwable::getCause)
                    .satisfies(cause -> assertThat(((Throwable) cause).getMessage()).contains("fail"));
        }
    }


}
