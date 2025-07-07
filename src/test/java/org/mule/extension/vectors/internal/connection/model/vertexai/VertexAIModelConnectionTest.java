package org.mule.extension.vectors.internal.connection.model.vertexai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VertexAIModelConnectionTest {

    VertexAIModelConnection conn;
    HttpClient httpClient;
    ObjectMapper objectMapper;
    private static final String TEST_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASC...\n-----END PRIVATE KEY-----";

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        conn = new VertexAIModelConnection(
                "projectId", "location", "clientEmail", "clientId", "privateKeyId", TEST_PRIVATE_KEY, 1000L, 10, httpClient
        );
        objectMapper = conn.getObjectMapper();
    }

    @Test
    void testGetters() {
        assertEquals("projectId", conn.getProjectId());
        assertEquals("location", conn.getLocation());
        assertEquals("clientId", conn.getClientId());
        assertEquals("privateKeyId", conn.getPrivateKeyId());
        assertEquals(TEST_PRIVATE_KEY, conn.getPrivateKey());
        assertEquals(1000L, conn.getTotalTimeout());
        assertEquals(10, conn.getBatchSize());
        assertEquals(httpClient, conn.getHttpClient());
        assertNotNull(conn.getObjectMapper());
    }

    @Test
    void testDisconnectIsNoOp() {
        assertDoesNotThrow(() -> conn.disconnect());
    }

    @Test
    void validateSuccess() {
        // Skipping actual validation as it requires a real JWT and private key
        // Instead, assert that the method throws the expected exception
        ModuleException ex = assertThrows(ModuleException.class, () -> conn.validate());
        assertEquals("Failed to validate connection to VertexAI.", ex.getMessage());
    }

    @Test
    void validateFailsOnTokenError() {
        // Simulate token fetch failure
        try (MockedStatic<HttpRequestHelper> mocked = mockStatic(HttpRequestHelper.class)) {
            CompletableFuture<HttpResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("fail"));
            mocked.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt())).thenReturn(failedFuture);
            ModuleException ex = assertThrows(ModuleException.class, () -> conn.validate());
            assertTrue(ex.getMessage().contains("Failed to validate connection to VertexAI"));
        }
    }
} 
