package org.mule.extension.vectors.internal.connection.embeddings.azureaivision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureAIVisionModelConnectionTest {
    @Mock HttpClient httpClient;
    AzureAIVisionModelConnection conn;

    @BeforeEach
    void setup() {
        conn = new AzureAIVisionModelConnection("https://endpoint", "key", "2023-04-01-preview", 1000L, httpClient);
    }

    @Test
    void getters_work() {
        assertEquals("https://endpoint", conn.getEndpoint());
        assertEquals("key", conn.getApiKey());
        assertEquals("2023-04-01-preview", conn.getApiVersion());
        assertEquals(1000L, conn.getTimeout());
        assertEquals(httpClient, conn.getHttpClient());
    }

    @Test
    void getEmbeddingModelService_returnsConstant() {
        assertEquals(Constants.EMBEDDING_MODEL_SERVICE_AZURE_AI_VISION, conn.getEmbeddingModelService());
    }

    @Test
    void disconnect_isNoOp() {
        assertDoesNotThrow(conn::disconnect);
    }

    @Test
    void validate_success_400Response() throws Exception {
        HttpResponse response = mock(HttpResponse.class);
        lenient().when(response.getStatusCode()).thenReturn(400);
        lenient().when(response.getEntity()).thenReturn(new ByteArrayHttpEntity("ok".getBytes()));
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), anyMap(), any(), anyInt()))
                    .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));
            assertDoesNotThrow(conn::validate);
        }
    }

    @Test
    void validate_non400_throwsModuleException() throws Exception {
        HttpResponse response = mock(HttpResponse.class);
        lenient().when(response.getStatusCode()).thenReturn(401);
        lenient().when(response.getEntity()).thenReturn(new ByteArrayHttpEntity("fail".getBytes()));
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), anyMap(), any(), anyInt()))
                    .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));
            ModuleException ex = assertThrows(ModuleException.class, conn::validate);
            assertEquals("Failed to validate connection to Azure AI Vision", ex.getMessage());
        }
    }

    @Test
    void validate_interruptedOrExecutionException_throwsModuleException() throws Exception {
        try (MockedStatic<HttpRequestHelper> helper = Mockito.mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), anyMap(), any(), anyInt()))
                    .thenReturn(java.util.concurrent.CompletableFuture.failedFuture(new RuntimeException("fail")));
            ModuleException ex = assertThrows(ModuleException.class, conn::validate);
            assertTrue(ex.getMessage().contains("Failed to validate connection"));
        }
    }

    @Test
    void buildUrlWithParams_encodesParams() {
        String url = conn.buildUrlWithParams("https://foo", Map.of("api-version", "2023-04-01-preview", "model-version", ""));
        assertTrue(url.contains("api-version=2023-04-01-preview"));
        assertTrue(url.contains("model-version="));
    }

    @Test
    void buildHeaders_containsApiKeyAndContentType() {
        Map<String, String> headers = conn.buildHeaders("application/json");
        assertEquals("application/json", headers.get("Content-Type"));
        assertEquals("key", headers.get("Ocp-Apim-Subscription-Key"));
    }

    @Test
    void handleErrorResponse_throwsModuleException() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(500);
        when(response.getEntity()).thenReturn(new ByteArrayHttpEntity("fail".getBytes()));
        ModuleException ex = assertThrows(ModuleException.class, () -> conn.handleErrorResponse(response, "msg"));
        assertTrue(ex.getMessage().contains("Azure AI Vision API error"));
    }
} 
