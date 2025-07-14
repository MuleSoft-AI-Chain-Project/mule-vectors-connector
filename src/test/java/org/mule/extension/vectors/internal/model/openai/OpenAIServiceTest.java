package org.mule.extension.vectors.internal.model.openai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mule.extension.vectors.internal.connection.embeddings.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.openai.OpenAIService;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OpenAIServiceTest {
    @Mock OpenAIModelConnection modelConnection;
    @Mock EmbeddingModelParameters modelParameters;
    @Mock HttpResponse httpResponse;
    OpenAIService service;

    @BeforeEach
    void setUp() {
        modelConnection = mock(OpenAIModelConnection.class);
        modelParameters = mock(EmbeddingModelParameters.class);
        httpResponse = mock(HttpResponse.class);
        when(modelConnection.getApiKey()).thenReturn("key");
        when(modelConnection.getHttpClient()).thenReturn(null);
        when(modelConnection.getTimeout()).thenReturn(1000L);
        when(modelParameters.getEmbeddingModelName()).thenReturn("test-model");
        service = new OpenAIService(modelConnection, modelParameters, 128);
    }

    @Test
    void embedTexts_success() throws Exception {
        List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
        String fakeResponse = "{" +
                "\"usage\": {\"total_tokens\": 42}," +
                "\"data\": [{\"embedding\": [0.1, 0.2, 0.3]}]" +
                "}";
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse httpResp = mock(HttpResponse.class);
            when(httpResp.getStatusCode()).thenReturn(200);
            when(httpResp.getEntity()).thenReturn(new ByteArrayHttpEntity(fakeResponse.getBytes(StandardCharsets.UTF_8)));
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(httpResp));
            Response<List<Embedding>> resp = service.embedTexts(segments);
            assertNotNull(resp);
            assertEquals(1, resp.content().size());
            assertThat(resp.content().get(0).vector()).containsExactly(0.1f, 0.2f, 0.3f);
        }
    }

    @Test
    void embedTexts_handlesErrorResponse() throws Exception {
        List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse httpResp = mock(HttpResponse.class);
            when(httpResp.getStatusCode()).thenReturn(500);
            when(httpResp.getEntity()).thenReturn(new ByteArrayHttpEntity("{\"error\":\"fail\"}".getBytes(StandardCharsets.UTF_8)));
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(httpResp));
            assertThatThrownBy(() -> service.embedTexts(segments))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to generate embeddings");
        }
    }

    @Test
    void embedTexts_handlesNullOrEmptyInput() {
        assertThrows(NullPointerException.class, () -> service.embedTexts(null));
        Response<List<Embedding>> resp = service.embedTexts(Collections.emptyList());
        assertNotNull(resp);
        assertTrue(resp.content().isEmpty());
    }

    @Test
    void generateTextEmbeddings_success() throws Exception {
        List<String> inputs = List.of("foo");
        String modelName = "test-model";
        String fakeResponse = "{" +
                "\"usage\": {\"total_tokens\": 42}," +
                "\"data\": [{\"embedding\": [0.1, 0.2, 0.3]}]" +
                "}";
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse httpResp = mock(HttpResponse.class);
            when(httpResp.getStatusCode()).thenReturn(200);
            when(httpResp.getEntity()).thenReturn(new ByteArrayHttpEntity(fakeResponse.getBytes(StandardCharsets.UTF_8)));
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(httpResp));
            Object resp = service.generateTextEmbeddings(inputs, modelName);
            assertTrue(resp instanceof String);
            assertTrue(((String) resp).contains("total_tokens"));
        }
    }

    @Test
    void generateTextEmbeddings_throwsOnNulls() {
        assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(null, "model"));
        assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(List.of(), "model"));
        assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(List.of("foo"), null));
        assertThrows(IllegalArgumentException.class, () -> service.generateTextEmbeddings(List.of("foo"), ""));
    }

    @Test
    void generateTextEmbeddings_propagatesModuleException() throws Exception {
        List<String> inputs = List.of("foo");
        String modelName = "test-model";
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenThrow(new ModuleException("fail", org.mule.extension.vectors.internal.error.MuleVectorsErrorType.AI_SERVICES_FAILURE));
            assertThatThrownBy(() -> service.generateTextEmbeddings(inputs, modelName))
                    .isInstanceOf(ModuleException.class)
                    .hasMessageContaining("fail");
        }
    }

    @Test
    void buildEmbeddingsPayload_serializesInput() throws Exception {
        List<String> inputs = List.of("foo", "bar");
        String modelName = "test-model";
        byte[] payload = service.buildEmbeddingsPayload(inputs, modelName);
        String json = new String(payload, StandardCharsets.UTF_8);
        assertTrue(json.contains("foo"));
        assertTrue(json.contains("bar"));
        assertTrue(json.contains("test-model"));
    }

    @Test
    void buildAuthHeaders_containsAuthorization() {
        Map<String, String> headers = service.buildAuthHeaders();
        assertEquals("Bearer key", headers.get("Authorization"));
    }
} 
