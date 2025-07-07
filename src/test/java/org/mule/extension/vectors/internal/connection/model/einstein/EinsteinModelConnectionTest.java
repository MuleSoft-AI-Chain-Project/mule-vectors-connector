package org.mule.extension.vectors.internal.connection.model.einstein;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mule.extension.vectors.internal.connection.model.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EinsteinModelConnectionTest {

    HttpClient httpClient;
    EinsteinModelConnection conn;

    @BeforeEach
    void setUp() throws Exception {
        httpClient = mock(HttpClient.class);
        // Mock getAccessTokenBlocking by subclassing and overriding
        conn = new EinsteinModelConnection("mydomain.my.salesforce.com", "client-id", "client-secret", httpClient) {
            @Override
            public String getAccessToken() { return "access-token"; }
            @Override
            public void setAccessToken(String newAccessToken) { /* no-op */ }
            @Override
            public CompletableFuture<String> getAccessTokenAsync() { return CompletableFuture.completedFuture("access-token"); }
        };
    }

    @Test
    void testGetters() {
        assertEquals(httpClient, conn.getHttpClient());
        assertEquals(Constants.EMBEDDING_MODEL_SERVICE_EINSTEIN, conn.getEmbeddingModelService());
        assertEquals("access-token", conn.getAccessToken());
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
                HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())
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
                HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())
            ).thenReturn(future);
            ModuleException ex = assertThrows(ModuleException.class, () -> conn.validate());
            assertEquals("Failed to validate connection to Einstein", ex.getMessage());
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
                HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())
            ).thenReturn(future);
            ModuleException ex = assertThrows(ModuleException.class, () -> conn.validate());
            assertEquals("Failed to validate connection to Einstein", ex.getMessage());
        }
    }
} 
