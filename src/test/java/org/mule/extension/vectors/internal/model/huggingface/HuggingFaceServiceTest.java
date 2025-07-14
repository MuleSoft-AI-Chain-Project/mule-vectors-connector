package org.mule.extension.vectors.internal.model.huggingface;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.embeddings.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.huggingface.HuggingFaceService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HuggingFaceServiceTest {
    @Mock
    HuggingFaceModelConnection connection;
    @Mock
    EmbeddingModelParameters params;
    HuggingFaceService service;

    @BeforeEach
    void setup() {
        service = new HuggingFaceService(connection, params, 512);
    }

    @Test
    void embedTexts_returnsEmbeddings() throws Exception {
        when(params.getEmbeddingModelName()).thenReturn("test-model");
        List<TextSegment> segments = List.of(dev.langchain4j.data.segment.TextSegment.from("foo"), dev.langchain4j.data.segment.TextSegment.from("bar"));
        String fakeResponse = "[[0.1,0.2],[0.3,0.4]]";
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusCode()).thenReturn(200);
        when(httpResponse.getEntity()).thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity(fakeResponse.getBytes(StandardCharsets.UTF_8)));
        when(connection.getTimeout()).thenReturn(1000L);
        when(connection.getApiKey()).thenReturn("key");
        when(connection.getHttpClient()).thenReturn(null);

        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), anyMap(), any(), anyInt()))
                    .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(httpResponse));
            Response<List<Embedding>> resp = service.embedTexts(segments);
            assertNotNull(resp);
            assertEquals(2, resp.content().size());
            assertEquals(0.1f, resp.content().get(0).vectorAsList().get(0));
            assertEquals(0.4f, resp.content().get(1).vectorAsList().get(1));
        }
    }

    @Test
    void embedTexts_throwsOnErrorResponse() throws Exception {
        when(params.getEmbeddingModelName()).thenReturn("test-model");
        List<TextSegment> segments = List.of(dev.langchain4j.data.segment.TextSegment.from("foo"));
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatusCode()).thenReturn(500);
        when(httpResponse.getEntity()).thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity("error".getBytes(StandardCharsets.UTF_8)));
        when(connection.getTimeout()).thenReturn(1000L);
        when(connection.getApiKey()).thenReturn("key");
        when(connection.getHttpClient()).thenReturn(null);

        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), anyMap(), any(), anyInt()))
                    .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(httpResponse));
            assertThrows(ModuleException.class, () -> service.embedTexts(segments));
        }
    }

    @Test
    void generateTextEmbeddings_throwsOnNullOrEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(null, "model"));
        assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(List.of(), "model"));
        assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(List.of("foo"), null));
        assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(List.of("foo"), " "));
    }

    @Test
    void buildEmbeddingsPayload_serializesInput() throws Exception {
        List<String> inputs = List.of("foo", "bar");
        byte[] payload = service.buildEmbeddingsPayload(inputs);
        String json = new String(payload, StandardCharsets.UTF_8);
        assertTrue(json.contains("foo"));
        assertTrue(json.contains("bar"));
        assertTrue(json.contains("inputs"));
    }

    @Test
    void buildAuthHeaders_containsAuthorization() {
        when(connection.getApiKey()).thenReturn("key");
        Map<String, String> headers = service.buildAuthHeaders();
        assertEquals("Bearer key", headers.get("Authorization"));
    }
} 
