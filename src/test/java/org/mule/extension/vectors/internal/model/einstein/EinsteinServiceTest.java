package org.mule.extension.vectors.internal.model.einstein;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.model.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EinsteinServiceTest {
    @Mock EinsteinModelConnection modelConnection;
    @Mock EmbeddingModelParameters modelParameters;
    EinsteinService service;

    @BeforeEach
    void setUp() {
        service = new EinsteinService(modelConnection, modelParameters, 1536);
    }

    @Test
    void buildEmbeddingsPayload_serializesInput() {
        List<String> texts = List.of("foo", "bar");
        String payload = invokeBuildEmbeddingsPayload(service, texts);
        assertTrue(payload.contains("foo"));
        assertTrue(payload.contains("bar"));
    }

    @Test
    void buildEmbeddingRequestUrl_encodesModelName() {
        String url = invokeBuildEmbeddingRequestUrl(service, "test-model");
        assertTrue(url.contains("test-model"));
        assertTrue(url.contains("/embeddings"));
    }

    @Test
    void buildEmbeddingRequestHeaders_containsAuthAndContentType() {
        when(modelConnection.getAccessToken()).thenReturn("token");
        Map<String, String> headers = invokeBuildEmbeddingRequestHeaders(service);
        assertEquals("Bearer token", headers.get("Authorization"));
        assertEquals("application/json;charset=utf-8", headers.get("Content-Type"));
        assertEquals("EinsteinGPT", headers.get("x-sfdc-app-context"));
        assertEquals("ai-platform-models-connected-app", headers.get("x-client-feature-id"));
    }

    @Test
    void embedTexts_returnsEmbeddings() throws Exception {
        List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
        when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
        when(modelConnection.getAccessToken()).thenReturn("token");
        when(modelConnection.getHttpClient()).thenReturn(null);
        String fakeResponse = "{" +
                "\"parameters\": {\"usage\": {\"total_tokens\": 42}}," +
                "\"embeddings\": [{\"embedding\": [0.1, 0.2, 0.3]}]" +
                "}";
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse httpResp = mock(HttpResponse.class);
            when(httpResp.getStatusCode()).thenReturn(200);
            when(httpResp.getEntity()).thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity(fakeResponse.getBytes(StandardCharsets.UTF_8)));
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(httpResp));
            Response<List<Embedding>> resp = service.embedTexts(segments);
            assertNotNull(resp);
            assertEquals(1, resp.content().size());
            assertNotNull(resp.tokenUsage());
        }
    }

    @Test
    void embedTexts_handlesErrorResponse() throws Exception {
        List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
        when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
        when(modelConnection.getAccessToken()).thenReturn("token");
        when(modelConnection.getHttpClient()).thenReturn(null);
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse httpResp = mock(HttpResponse.class);
            when(httpResp.getStatusCode()).thenReturn(500);
            when(httpResp.getEntity()).thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity("fail".getBytes(StandardCharsets.UTF_8)));
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(httpResp));
            assertThatThrownBy(() -> service.embedTexts(segments))
                    .isInstanceOf(ModuleException.class)
                    .hasMessageContaining("Einstein API");
        }
    }

    @Test
    void embedTexts_handlesTokenRefresh() throws Exception {
        List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
        when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
        when(modelConnection.getAccessToken()).thenReturn("token");
        when(modelConnection.getHttpClient()).thenReturn(null);
        when(modelConnection.getAccessTokenAsync()).thenReturn(CompletableFuture.completedFuture("newtoken"));
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse httpResp = mock(HttpResponse.class);
            when(httpResp.getStatusCode()).thenReturn(401);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(httpResp))
                .thenReturn(CompletableFuture.completedFuture(httpResp));
            assertThrows(org.mule.runtime.extension.api.exception.ModuleException.class, () -> service.embedTexts(segments));
        }
    }

    @Test
    void generateTextEmbeddings_propagatesModuleException() {
        when(modelConnection.getAccessToken()).thenReturn("token");
        when(modelConnection.getHttpClient()).thenReturn(null);
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.failedFuture(new ModuleException("fail", MuleVectorsErrorType.AI_SERVICES_FAILURE)));
            assertThatThrownBy(() -> service.generateTextEmbeddings(List.of("foo"), "bar"))
                    .isInstanceOf(ModuleException.class)
                    .hasMessageContaining("fail");
        }
    }

    // Reflection helpers for private methods
    private String invokeBuildEmbeddingsPayload(EinsteinService service, List<String> texts) {
        try {
            var m = EinsteinService.class.getDeclaredMethod("buildEmbeddingsPayload", List.class);
            m.setAccessible(true);
            return (String) m.invoke(service, texts);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private String invokeBuildEmbeddingRequestUrl(EinsteinService service, String modelName) {
        try {
            var m = EinsteinService.class.getDeclaredMethod("buildEmbeddingRequestUrl", String.class);
            m.setAccessible(true);
            return (String) m.invoke(service, modelName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private Map<String, String> invokeBuildEmbeddingRequestHeaders(EinsteinService service) {
        try {
            var m = EinsteinService.class.getDeclaredMethod("buildEmbeddingRequestHeaders");
            m.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) m.invoke(service);
            return headers;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 