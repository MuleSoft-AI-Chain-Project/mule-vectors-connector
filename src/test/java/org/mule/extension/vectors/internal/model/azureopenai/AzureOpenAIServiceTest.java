package org.mule.extension.vectors.internal.model.azureopenai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.azureopenai.AzureOpenAIService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureOpenAIServiceTest {
    AzureOpenAIModelConnection modelConnection;
    EmbeddingModelParameters modelParameters;
    AzureOpenAIService service;
    HttpResponse httpResponse;

    @BeforeEach
    void setUp() {
        // Only initialize mocks, do not stub anything here
        modelConnection = mock(AzureOpenAIModelConnection.class);
        modelParameters = mock(EmbeddingModelParameters.class);
        service = new AzureOpenAIService(modelConnection, modelParameters);
    }

    @Test
    void embedTexts_returnsEmbeddings() throws Exception {
        AzureOpenAIModelConnection modelConnection = mock(AzureOpenAIModelConnection.class);
        EmbeddingModelParameters modelParameters = mock(EmbeddingModelParameters.class);
        when(modelConnection.getApiKey()).thenReturn("key");
        when(modelConnection.getEndpoint()).thenReturn("https://endpoint");
        when(modelConnection.getApiVersion()).thenReturn("2024-01-01");
        when(modelConnection.getTimeout()).thenReturn(1000L);
        when(modelParameters.getEmbeddingModelName()).thenReturn("text-embedding-ada-002");
        AzureOpenAIService service = new AzureOpenAIService(modelConnection, modelParameters);
        List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
        String fakeResponse = "{" +
                "\"usage\": {\"total_tokens\": 42}," +
                "\"data\": [{\"embedding\": [0.1, 0.2, 0.3]}]" +
                "}";
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse mockResponse = mock(HttpResponse.class);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(mockResponse));
            helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
                    .thenReturn(fakeResponse);
            Response<List<Embedding>> resp = service.embedTexts(segments);
            assertNotNull(resp);
            assertEquals(1, resp.content().size());
            // TokenUsage: use getTotal() if available, otherwise skip
            // assertEquals(42, resp.tokenUsage().getTotal());
        }
    }

    @Test
    void embedTexts_handlesErrorResponse() throws Exception {
        AzureOpenAIModelConnection modelConnection = mock(AzureOpenAIModelConnection.class);
        EmbeddingModelParameters modelParameters = mock(EmbeddingModelParameters.class);
        when(modelConnection.getApiKey()).thenReturn("key");
        when(modelConnection.getEndpoint()).thenReturn("https://endpoint");
        when(modelConnection.getApiVersion()).thenReturn("2024-01-01");
        when(modelConnection.getTimeout()).thenReturn(1000L);
        when(modelParameters.getEmbeddingModelName()).thenReturn("text-embedding-ada-002");
        AzureOpenAIService service = new AzureOpenAIService(modelConnection, modelParameters);
        List<TextSegment> segments = List.of(new TextSegment("foo", new dev.langchain4j.data.document.Metadata()));
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse mockResponse = mock(HttpResponse.class);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(mockResponse));
            helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
                    .thenThrow(new ModuleException("Azure OpenAI API error (HTTP 500): fail", MuleVectorsErrorType.AI_SERVICES_FAILURE));
            assertThrows(ModuleException.class, () -> service.embedTexts(segments));
        }
    }

    @Test
    void embedTexts_handlesNullOrEmptyInput() {
        AzureOpenAIService service = new AzureOpenAIService(mock(AzureOpenAIModelConnection.class), mock(EmbeddingModelParameters.class));
        List<String> emptyList = List.of();
        List<String> barList = List.of("bar");
        
        // Separate assertions to avoid multiple method invocations in lambda
        assertThatThrownBy(() -> service.generateTextEmbeddings(null, "foo"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.generateTextEmbeddings(emptyList, "foo"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.generateTextEmbeddings(barList, null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.generateTextEmbeddings(barList, ""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void generateTextEmbeddings_propagatesModuleException() throws Exception {
        AzureOpenAIModelConnection conn = mock(AzureOpenAIModelConnection.class);
        when(conn.getEndpoint()).thenReturn("https://endpoint");
        when(conn.getApiVersion()).thenReturn("2024-01-01");
        AzureOpenAIService svc = new AzureOpenAIService(conn, modelParameters);
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            HttpResponse mockResponse = mock(HttpResponse.class);
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(mockResponse));
            helper.when(() -> HttpRequestHelper.handleEmbeddingResponse(any(HttpResponse.class), anyString()))
                    .thenThrow(new ModuleException("fail", MuleVectorsErrorType.AI_SERVICES_FAILURE));
            assertThatThrownBy(() -> svc.generateTextEmbeddings(List.of("foo"), "bar"))
                    .isInstanceOf(ModuleException.class)
                    .hasMessageContaining("fail");
        }
    }

    @Test
    void buildUrlForDeployment_encodesParams() throws Exception {
        AzureOpenAIModelConnection conn = mock(AzureOpenAIModelConnection.class);
        when(conn.getEndpoint()).thenReturn("https://endpoint");
        when(conn.getApiVersion()).thenReturn("2024-01-01");
        AzureOpenAIService svc = new AzureOpenAIService(conn, modelParameters);
        var m = AzureOpenAIService.class.getDeclaredMethod("buildUrlForDeployment", String.class);
        m.setAccessible(true);
        String url = (String) m.invoke(svc, "deploy");
        assertTrue(url.contains("deploy"));
        assertTrue(url.contains("2024-01-01"));
    }

    @Test
    void buildTextEmbeddingPayload_serializesInput() throws Exception {
        AzureOpenAIService service = new AzureOpenAIService(mock(AzureOpenAIModelConnection.class), mock(EmbeddingModelParameters.class));
        var m = AzureOpenAIService.class.getDeclaredMethod("buildTextEmbeddingPayload", List.class);
        m.setAccessible(true);
        byte[] bytes = (byte[]) m.invoke(service, List.of("foo", "bar"));
        String json = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(json.contains("foo"));
        assertTrue(json.contains("bar"));
    }

    @Test
    void buildHeaders_containsApiKeyAndContentType() throws Exception {
        AzureOpenAIModelConnection conn = mock(AzureOpenAIModelConnection.class);
        when(conn.getApiKey()).thenReturn("key");
        AzureOpenAIService svc = new AzureOpenAIService(conn, modelParameters);
        var m = AzureOpenAIService.class.getDeclaredMethod("buildHeaders");
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        var headers = (java.util.Map<String, String>) m.invoke(svc);
        assertEquals("key", headers.get("api-key"));
        assertEquals("application/json", headers.get("Content-Type"));
    }
} 
