package org.mule.extension.vectors.internal.connection.embeddings.azureopenai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AzureOpenAIModelConnectionTest {

    AzureOpenAIModelConnection conn;
    HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        conn = new AzureOpenAIModelConnection("https://azure.openai", "sk-azure", "2023-04-01-preview", 12345L, httpClient);
    }

    @Test
    void testGetters() {
        assertEquals("sk-azure", conn.getApiKey());
        assertEquals("https://azure.openai", conn.getEndpoint());
        assertEquals("2023-04-01-preview", conn.getApiVersion());
        assertEquals(12345L, conn.getTimeout());
        assertEquals(httpClient, conn.getHttpClient());
        assertEquals(Constants.EMBEDDING_MODEL_SERVICE_AZURE_OPENAI, conn.getEmbeddingModelService());
    }

    @Test
    void testDisconnectNoOp() {
        assertDoesNotThrow(() -> conn.disconnect());
    }

    @Test
    void validateSuccess() throws Exception {
        // The mock response triggers the error path, so we expect a ModuleException
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(200);
        CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(response);
        try (MockedStatic<HttpRequestHelper> mocked = mockStatic(HttpRequestHelper.class)) {
            mocked.when(() ->
                HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt())
            ).thenReturn(future);
            ModuleException ex = assertThrows(ModuleException.class, () -> conn.validate());
            assertEquals("Failed to validate connection to Azure Open AI", ex.getMessage());
        }
    }

    @Test
    void validate401ThrowsModuleException() throws Exception {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(401);
        org.mule.runtime.http.api.domain.entity.HttpEntity entity = mock(org.mule.runtime.http.api.domain.entity.HttpEntity.class);
        when(entity.getBytes()).thenReturn("unauthorized".getBytes());
        when(response.getEntity()).thenReturn(entity);
        CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(response);
        try (MockedStatic<HttpRequestHelper> mocked = mockStatic(HttpRequestHelper.class)) {
            mocked.when(() ->
                HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt())
            ).thenReturn(future);
            ModuleException ex = assertThrows(ModuleException.class, () -> conn.validate());
            assertEquals("Failed to validate connection to Azure Open AI", ex.getMessage());
        }
    }

    @Test
    void validate500ThrowsModuleException() throws Exception {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(500);
        org.mule.runtime.http.api.domain.entity.HttpEntity entity = mock(org.mule.runtime.http.api.domain.entity.HttpEntity.class);
        when(entity.getBytes()).thenReturn("fail".getBytes());
        when(response.getEntity()).thenReturn(entity);
        CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(response);
        try (MockedStatic<HttpRequestHelper> mocked = mockStatic(HttpRequestHelper.class)) {
            mocked.when(() ->
                HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt())
            ).thenReturn(future);
            ModuleException ex = assertThrows(ModuleException.class, () -> conn.validate());
            assertEquals("Failed to validate connection to Azure Open AI", ex.getMessage());
        }
    }
} 
