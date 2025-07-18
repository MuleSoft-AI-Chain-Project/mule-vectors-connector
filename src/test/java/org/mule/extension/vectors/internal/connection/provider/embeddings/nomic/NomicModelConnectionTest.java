package org.mule.extension.vectors.internal.connection.embeddings.nomic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NomicModelConnectionTest {

    NomicModelConnection conn;
    HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        conn = new NomicModelConnection("sk-nomic", 12345L, httpClient);
    }

    @Test
    void testGetters() {
        assertEquals("sk-nomic", conn.getApiKey());
        assertEquals(12345L, conn.getTimeout());
        assertEquals(httpClient, conn.getHttpClient());
        assertEquals(Constants.EMBEDDING_MODEL_SERVICE_NOMIC, conn.getEmbeddingModelService());
    }

    @Test
    void testDisconnectNoOp() {
        assertDoesNotThrow(() -> conn.disconnect());
    }

    @Test
    void validateSuccess() throws Exception {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(200);
        CompletableFuture<HttpResponse> future = CompletableFuture.completedFuture(response);
        try (MockedStatic<HttpRequestHelper> mocked = mockStatic(HttpRequestHelper.class)) {
            mocked.when(() ->
                HttpRequestHelper.executePostRequest(any(), anyString(), any(), any(), anyInt())
            ).thenReturn(future);
            assertDoesNotThrow(() -> conn.validate());
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
            assertEquals("Failed to validate connection to Nomic", ex.getMessage());
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
            assertEquals("Failed to validate connection to Nomic", ex.getMessage());
        }
    }
} 
