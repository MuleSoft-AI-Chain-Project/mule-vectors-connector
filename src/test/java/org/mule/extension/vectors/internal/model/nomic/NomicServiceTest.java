package org.mule.extension.vectors.internal.model.nomic;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.nomic.NomicService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NomicServiceTest {
    @Mock NomicModelConnection modelConnection;
    @Mock EmbeddingModelParameters modelParameters;
    NomicService service;

    @BeforeEach
    void setUp() {
        service = new NomicService(modelConnection, modelParameters);
    }

    @Test
    void embedTexts_returnsEmbeddings() throws Exception {
        when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
        String embeddingJson = "{" +
                "\"embeddings\": [[0.1, 0.2, 0.3]]," +
                "\"usage\": {\"total_tokens\": 42}" +
                "}";
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            when(modelConnection.getHttpClient()).thenReturn(null);
            when(modelConnection.getApiKey()).thenReturn("key");
            when(modelConnection.getTimeout()).thenReturn(1000L);
            HttpResponse mockResponse = mock(HttpResponse.class);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(mockResponse));
            helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
                    .thenReturn(embeddingJson);
            List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
            Response<List<Embedding>> resp = service.embedTexts(segments);
            assertNotNull(resp);
            assertEquals(1, resp.content().size());
            float[] expected = new float[]{0.1f, 0.2f, 0.3f};
            assertArrayEquals(expected, resp.content().get(0).vector(), 1e-6f);
        }
    }

    @Test
    void embedTexts_handlesErrorResponse() throws Exception {
        when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
        String errorJson = "{\"error\":\"fail\"}";
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            when(modelConnection.getHttpClient()).thenReturn(null);
            when(modelConnection.getApiKey()).thenReturn("key");
            when(modelConnection.getTimeout()).thenReturn(1000L);
            HttpResponse mockResponse = mock(HttpResponse.class);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(mockResponse));
            helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
                    .thenThrow(new ModuleException("Nomic API error (HTTP 500): " + errorJson, org.mule.extension.vectors.internal.error.MuleVectorsErrorType.AI_SERVICES_FAILURE));
            List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
            assertThrows(ModuleException.class, () -> service.embedTexts(segments));
        }
    }

    @Test
    void embedTexts_handlesNullOrEmptyInput() {
        List<TextSegment> emptyList = Collections.emptyList();
        assertThrows(NullPointerException.class, () -> service.embedTexts(null));
        assertThrows(NullPointerException.class, () -> service.embedTexts(emptyList));
    }

    @Test
    void generateTextEmbeddings_normal() throws Exception {
        when(modelConnection.getHttpClient()).thenReturn(null);
        when(modelConnection.getApiKey()).thenReturn("key");
        when(modelConnection.getTimeout()).thenReturn(1000L);
        String embeddingJson = "{" +
                "\"embeddings\": [[0.1, 0.2, 0.3]]," +
                "\"usage\": {\"total_tokens\": 42}" +
                "}";
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse mockResponse = mock(HttpResponse.class);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(mockResponse));
            helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
                    .thenReturn(embeddingJson);
            Object result = service.generateTextEmbeddings(List.of("foo"), "test-model");
            assertTrue(result instanceof String);
            JSONObject json = new JSONObject((String) result);
            JSONArray arr = json.getJSONArray("embeddings");
            assertEquals(1, arr.length());
        }
    }

    @Test
    void generateTextEmbeddings_propagatesModuleException() throws Exception {
        when(modelConnection.getHttpClient()).thenReturn(null);
        when(modelConnection.getApiKey()).thenReturn("key");
        when(modelConnection.getTimeout()).thenReturn(1000L);
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse mockResponse = mock(HttpResponse.class);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(mockResponse));
            helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
                    .thenThrow(new ModuleException("fail", org.mule.extension.vectors.internal.error.MuleVectorsErrorType.AI_SERVICES_FAILURE));
            assertThrows(ModuleException.class, () -> service.generateTextEmbeddings(List.of("foo"), "test-model"));
        }
    }

    @Test
    void buildTextEmbeddingsPayload_serializesInput() throws Exception {
        var m = NomicService.class.getDeclaredMethod("buildTextEmbeddingsPayload", List.class, String.class);
        m.setAccessible(true);
        byte[] bytes = (byte[]) m.invoke(service, List.of("foo"), "bar");
        String json = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(json.contains("foo"));
        assertTrue(json.contains("bar"));
    }

    @Test
    void buildAuthHeaders_containsAuthorization() throws Exception {
        when(modelConnection.getApiKey()).thenReturn("key");
        var m = NomicService.class.getDeclaredMethod("buildAuthHeaders");
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) m.invoke(service);
        assertEquals("Bearer key", headers.get("Authorization"));
    }
} 
