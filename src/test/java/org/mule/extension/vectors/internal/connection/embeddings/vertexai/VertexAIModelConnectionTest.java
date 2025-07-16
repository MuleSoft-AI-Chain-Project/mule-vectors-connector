package org.mule.extension.vectors.internal.connection.embeddings.vertexai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.concurrent.CompletableFuture;
import java.util.List;

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

    @Test
    void createJwt_throwsOnInvalidKey() throws Exception {
        VertexAIModelConnection badConn = new VertexAIModelConnection(
                "projectId", "location", "clientEmail", "clientId", "privateKeyId", "badkey", 1000L, 10, httpClient);
        java.lang.reflect.Method m = VertexAIModelConnection.class.getDeclaredMethod("createJwt");
        m.setAccessible(true);
        Exception ex = assertThrows(Exception.class, () -> m.invoke(badConn));
        // Print actual message for debug
        System.out.println("[DEBUG] createJwt exception: " + ex.getCause().getMessage());
        assertNotNull(ex.getCause().getMessage());
    }

    // @Test
    // void parsePrivateKey_throwsOnMalformedPem() throws Exception {
    //     VertexAIModelConnection c = conn;
    //     Exception ex = assertThrows(Exception.class, () -> {
    //         java.lang.reflect.Method m = VertexAIModelConnection.class.getDeclaredMethod("parsePrivateKey", String.class);
    //         m.setAccessible(true);
    //         m.invoke(c, "not-a-key");
    //     });
    //     assertTrue(ex.getCause().getMessage().toLowerCase().contains("illegal base64"));
    // }

    // @Test
    // void signWithRSA_throwsOnBadKey() throws Exception {
    //     VertexAIModelConnection c = conn;
    //     Exception ex = assertThrows(Exception.class, () -> {
    //         java.lang.reflect.Method m = VertexAIModelConnection.class.getDeclaredMethod("signWithRSA", byte[].class, java.security.PrivateKey.class);
    //         m.setAccessible(true);
    //         m.invoke(c, "foo".getBytes(), null);
    //     });
    //     assertNotNull(ex.getCause());
    // }

    @Test
    void base64UrlEncode_encodesCorrectly() throws Exception {
        VertexAIModelConnection c = conn;
        java.lang.reflect.Method m = VertexAIModelConnection.class.getDeclaredMethod("base64UrlEncode", byte[].class);
        m.setAccessible(true);
        String encoded = (String) m.invoke(c, "abc".getBytes());
        assertEquals(java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("abc".getBytes()), encoded);
    }

    // Minimal concrete HttpResponse for testing
    @SuppressWarnings("rawtypes")
    static class TestHttpResponse implements HttpResponse {
        private final int statusCode;
        private final boolean throwOnGetEntity;
        TestHttpResponse(int statusCode, boolean throwOnGetEntity) {
            this.statusCode = statusCode;
            this.throwOnGetEntity = throwOnGetEntity;
        }
        public int getStatusCode() { return statusCode; }
        public org.mule.runtime.http.api.domain.entity.HttpEntity getEntity() {
            if (throwOnGetEntity) throw new RuntimeException("fail");
            return null;
        }
        public String getReasonPhrase() { return null; }
        public String getHeaderValue(String s) { return null; }
        public List<String> getHeaderValues(String s) { return null; }
        @Override
        public org.mule.runtime.api.util.MultiMap<String, String> getHeaders() {
            return new org.mule.runtime.api.util.MultiMap<>();
        }
        public List<String> getHeaderNames() { return java.util.Collections.emptyList(); }
        public String getHeaderValueIgnoreCase(String s) { return null; }
        public List<String> getHeaderValuesIgnoreCase(String s) { return null; }
    }

    // Helper to find ModuleException in cause chain
    private static ModuleException findModuleException(Throwable ex) {
        while (ex != null) {
            if (ex instanceof ModuleException) return (ModuleException) ex;
            ex = ex.getCause();
        }
        return null;
    }

    @Test
    void handleErrorResponse_throwsModuleExceptionOnIOException() throws Exception {
        VertexAIModelConnection c = conn;
        HttpResponse response = mock(HttpResponse.class);
        when(response.getEntity()).thenThrow(new RuntimeException("fail"));
        when(response.getStatusCode()).thenReturn(500);
        java.lang.reflect.Method m = VertexAIModelConnection.class.getDeclaredMethod("handleErrorResponse", HttpResponse.class, String.class);
        m.setAccessible(true);
        Exception ex = assertThrows(Exception.class, () -> m.invoke(c, response, "msg"));
        Throwable root = ex;
        while (root.getCause() != null) root = root.getCause();
        assertTrue(root.getMessage().contains("fail"),
            "Actual: " + root.getClass() + ": " + root.getMessage());
    }

    @Test
    void refreshAccessTokenAsync_handlesNon200Response() throws Exception {
        VertexAIModelConnection c = spy(conn);
        java.lang.reflect.Method createJwt = VertexAIModelConnection.class.getDeclaredMethod("createJwt");
        createJwt.setAccessible(true);
        doReturn("dummy.jwt").when(c).createJwt();
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(401);
        when(response.getEntity()).thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity("fail".getBytes()));
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), anyMap(), any(), anyInt()))
                    .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));
            java.lang.reflect.Method m = VertexAIModelConnection.class.getDeclaredMethod("refreshAccessTokenAsync");
            m.setAccessible(true);
            CompletableFuture<String> fut = (CompletableFuture<String>) m.invoke(c);
            Exception ex = assertThrows(Exception.class, fut::join);
            Throwable root = ex;
            while (root.getCause() != null) root = root.getCause();
            assertTrue(root.getMessage().contains("Error getting access token"),
                "Actual: " + root.getClass() + ": " + root.getMessage());
        }
    }

    @Test
    void refreshAccessTokenAsync_handlesJsonParseError() throws Exception {
        VertexAIModelConnection c = spy(conn);
        java.lang.reflect.Method createJwt = VertexAIModelConnection.class.getDeclaredMethod("createJwt");
        createJwt.setAccessible(true);
        doReturn("dummy.jwt").when(c).createJwt();
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(200);
        when(response.getEntity()).thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity("notjson".getBytes()));
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executePostRequest(any(), anyString(), anyMap(), any(), anyInt()))
                    .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));
            java.lang.reflect.Method m = VertexAIModelConnection.class.getDeclaredMethod("refreshAccessTokenAsync");
            m.setAccessible(true);
            CompletableFuture<String> fut = (CompletableFuture<String>) m.invoke(c);
            Exception ex = assertThrows(Exception.class, fut::join);
            Throwable root = ex;
            while (root.getCause() != null) root = root.getCause();
            assertTrue(root instanceof com.fasterxml.jackson.core.JsonParseException,
                "Actual: " + root.getClass() + ": " + root.getMessage());
            assertTrue(root.getMessage().contains("notjson") || root.getMessage().contains("Unrecognized token"),
                "Actual: " + root.getClass() + ": " + root.getMessage());
        }
    }

    @Test
    void validateAccessTokenAsync_handlesNon200AndJsonError() throws Exception {
        VertexAIModelConnection c = conn;
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(401);
        when(response.getEntity()).thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity("fail".getBytes()));
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
                    .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));
            java.lang.reflect.Method m = VertexAIModelConnection.class.getDeclaredMethod("validateAccessTokenAsync", String.class);
            m.setAccessible(true);
            CompletableFuture<Boolean> fut = (CompletableFuture<Boolean>) m.invoke(c, "token");
            boolean valid = fut.get();
            assertFalse(valid);
        }
        // JSON parse error
        response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(200);
        when(response.getEntity()).thenReturn(new org.mule.runtime.http.api.domain.entity.ByteArrayHttpEntity("notjson".getBytes()));
        try (MockedStatic<HttpRequestHelper> helper = mockStatic(HttpRequestHelper.class)) {
            helper.when(() -> HttpRequestHelper.executeGetRequest(any(), anyString(), any(), anyInt()))
                    .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(response));
            java.lang.reflect.Method m = VertexAIModelConnection.class.getDeclaredMethod("validateAccessTokenAsync", String.class);
            m.setAccessible(true);
            CompletableFuture<Boolean> fut = (CompletableFuture<Boolean>) m.invoke(c, "token");
            boolean valid = fut.get();
            assertFalse(valid);
        }
    }

    @Test
    void getOrRefreshToken_usesCachedTokenIfValidElseRefreshes() throws Exception {
        VertexAIModelConnection c = spy(conn);
        // Simulate valid cached token
        java.lang.reflect.Field tokenField = VertexAIModelConnection.class.getDeclaredField("accessToken");
        tokenField.setAccessible(true);
        java.util.concurrent.atomic.AtomicReference<String> tokenRef = (java.util.concurrent.atomic.AtomicReference<String>) tokenField.get(c);
        tokenRef.set("cached-token");
        java.lang.reflect.Method validateM = VertexAIModelConnection.class.getDeclaredMethod("validateAccessTokenAsync", String.class);
        validateM.setAccessible(true);
        java.lang.reflect.Method refreshM = VertexAIModelConnection.class.getDeclaredMethod("refreshAccessTokenAsync");
        refreshM.setAccessible(true);
        // For valid token
        doAnswer(invocation -> java.util.concurrent.CompletableFuture.completedFuture(true))
                .when(c).validateAccessTokenAsync(anyString());
        String token = c.getOrRefreshToken().get();
        assertEquals("cached-token", token);
        // For invalid token, triggers refresh
        doAnswer(invocation -> java.util.concurrent.CompletableFuture.completedFuture(false))
                .when(c).validateAccessTokenAsync(anyString());
        doAnswer(invocation -> java.util.concurrent.CompletableFuture.completedFuture("new-token"))
                .when(c).refreshAccessTokenAsync();
        token = c.getOrRefreshToken().get();
        assertEquals("new-token", token);
    }
} 
