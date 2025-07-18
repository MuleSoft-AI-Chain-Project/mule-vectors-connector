package org.mule.extension.vectors.internal.connection.provider.embeddings.openai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.embeddings.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.openai.OpenAIModelConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.embeddings.openai.OpenAIModelConnectionProvider;
import org.mule.runtime.http.api.client.HttpClient;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenAIModelConnectionProviderTest {

    OpenAIModelConnectionProvider provider;
    OpenAIModelConnectionParameters params;
    HttpClient httpClient;

    @BeforeEach
    void setup() throws Exception {
        provider = new OpenAIModelConnectionProvider();
        params = mock(OpenAIModelConnectionParameters.class);
        httpClient = mock(HttpClient.class);
        // Inject mocks into private fields
        Field paramsField = OpenAIModelConnectionProvider.class.getDeclaredField("openAIModelConnectionParameters");
        paramsField.setAccessible(true);
        paramsField.set(provider, params);
        Field httpClientField = provider.getClass().getSuperclass().getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(provider, httpClient);
    }

    @Test
    void connectReturnsValidConnection() throws Exception {
        when(params.getApiKey()).thenReturn("sk-test");
        when(params.getTimeout()).thenReturn(12345L);
        var conn = provider.connect();
        assertTrue(conn instanceof OpenAIModelConnection);
        OpenAIModelConnection oaiConn = (OpenAIModelConnection) conn;
        assertEquals("sk-test", oaiConn.getApiKey());
        assertEquals(12345L, oaiConn.getTimeout());
        assertEquals(httpClient, oaiConn.getHttpClient());
    }

    @Test
    void connectHandlesNullApiKey() throws Exception {
        when(params.getApiKey()).thenReturn(null);
        when(params.getTimeout()).thenReturn(1000L);
        var conn = provider.connect();
        assertNull(((OpenAIModelConnection) conn).getApiKey());
    }

    @Test
    void connectHandlesZeroTimeout() throws Exception {
        when(params.getApiKey()).thenReturn("sk");
        when(params.getTimeout()).thenReturn(0L);
        var conn = provider.connect();
        assertEquals(0L, ((OpenAIModelConnection) conn).getTimeout());
    }

    @Test
    void connectHandlesNegativeTimeout() throws Exception {
        when(params.getApiKey()).thenReturn("sk");
        when(params.getTimeout()).thenReturn(-1L);
        var conn = provider.connect();
        assertEquals(-1L, ((OpenAIModelConnection) conn).getTimeout());
    }
} 
