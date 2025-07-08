package org.mule.extension.vectors.internal.connection.store.weaviate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeaviateStoreConnectionTest {
    WeaviateStoreConnectionParameters params;
    HttpClient httpClient;
    WeaviateStoreConnection conn;

    @BeforeEach
    void setUp() {
        params = mock(WeaviateStoreConnectionParameters.class);
        when(params.getScheme()).thenReturn("https");
        when(params.getHost()).thenReturn("localhost");
        when(params.getPort()).thenReturn(8181);
        when(params.isSecuredGrpc()).thenReturn(true);
        when(params.getGrpcPort()).thenReturn(50051);
        when(params.isUseGrpcForInserts()).thenReturn(true);
        when(params.getApiKey()).thenReturn("secret");
        when(params.isAvoidDups()).thenReturn(true);
        when(params.getConsistencyLevel()).thenReturn("ALL");
        httpClient = mock(HttpClient.class);
        conn = new WeaviateStoreConnection(params, httpClient);
    }

    @Test
    void getters_work() {
        assertEquals("https", conn.getScheme());
        assertEquals("localhost", conn.getHost());
        assertEquals(8181, conn.getPort());
        assertTrue(conn.isSecuredGrpc());
        assertEquals(50051, conn.getGrpcPort());
        assertTrue(conn.isUseGrpcForInserts());
        assertEquals("secret", conn.getApikey());
        assertTrue(conn.isAvoidDups());
        assertEquals("ALL", conn.getConsistencyLevel());
        assertEquals(params, conn.getConnectionParameters());
        assertEquals(httpClient, conn.getHttpClient());
    }

    @Test
    void getVectorStore_returnsConstant() {
        assertEquals("WEAVIATE", conn.getVectorStore());
    }

    @Test
    void disconnect_noop() {
        conn.disconnect(); // should not throw
    }

    @Test
    void buildHeaders_setsAuthContentTypeAccept() throws Exception {
        java.lang.reflect.Method m = WeaviateStoreConnection.class.getDeclaredMethod("buildHeaders", String.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) m.invoke(conn, "application/json");
        assertEquals("Bearer secret", headers.get("Authorization"));
        assertEquals("application/json", headers.get("Content-Type"));
        assertEquals("application/json", headers.get("Accept"));
    }

    @Test
    void validate_success() throws Exception {
        try (var helper = mockStatic(org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.class)) {
            HttpResponse response = mock(HttpResponse.class);
            when(response.getStatusCode()).thenReturn(200);
            when(response.getEntity()).thenReturn(new ByteArrayHttpEntity("ok".getBytes()));
            helper.when(() -> org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(response));
            conn.validate(); // should not throw
        }
    }

    @Test
    void validate_missingScheme() {
        when(params.getScheme()).thenReturn(null);
        ModuleException ex = assertThrows(ModuleException.class, () -> new WeaviateStoreConnection(params, httpClient).validate());
        assertTrue(ex.getMessage().contains("Scheme is required"));
    }

    @Test
    void validate_missingHost() {
        when(params.getHost()).thenReturn(null);
        ModuleException ex = assertThrows(ModuleException.class, () -> new WeaviateStoreConnection(params, httpClient).validate());
        assertTrue(ex.getMessage().contains("Host is required"));
    }

    @Test
    void validate_missingApiKey() {
        when(params.getApiKey()).thenReturn(null);
        ModuleException ex = assertThrows(ModuleException.class, () -> new WeaviateStoreConnection(params, httpClient).validate());
        assertTrue(ex.getMessage().contains("API Key is required"));
    }

    @Test
    void validate_httpError() {
        try (var helper = mockStatic(org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.class)) {
            HttpResponse response = mock(HttpResponse.class);
            when(response.getStatusCode()).thenReturn(401);
            when(response.getEntity()).thenReturn(new ByteArrayHttpEntity("fail".getBytes()));
            helper.when(() -> org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(response));
            ModuleException ex = assertThrows(ModuleException.class, conn::validate);
            String msg = ex.getMessage();
            assertTrue(msg.contains("Failed to connect to Weaviate store"), "Actual: " + msg);
        }
    }

    @Test
    void validate_errorReadingBody() {
        try (var helper = mockStatic(org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.class)) {
            HttpResponse response = mock(HttpResponse.class);
            when(response.getStatusCode()).thenReturn(401);
            when(response.getEntity()).thenThrow(new RuntimeException("fail"));
            helper.when(() -> org.mule.extension.vectors.internal.helper.request.HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
                    .thenReturn(CompletableFuture.completedFuture(response));
            ModuleException ex = assertThrows(ModuleException.class, conn::validate);
            assertTrue(ex.getMessage().toLowerCase().contains("fail"));
        }
    }
} 
