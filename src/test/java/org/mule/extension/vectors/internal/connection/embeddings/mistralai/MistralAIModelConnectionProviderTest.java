package org.mule.extension.vectors.internal.connection.embeddings.mistralai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.runtime.http.api.client.HttpClient;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MistralAIModelConnectionProviderTest {

    MistralAIModelConnectionProvider provider;
    MistralAIModelConnectionParameters params;
    HttpClient httpClient;

    @BeforeEach
    void setUp() throws Exception {
        provider = new MistralAIModelConnectionProvider();
        params = mock(MistralAIModelConnectionParameters.class);
        // Only inject params into provider
        Field paramsField = MistralAIModelConnectionProvider.class.getDeclaredField("mistralAIModelConnectionParameters");
        paramsField.setAccessible(true);
        paramsField.set(provider, params);
    }

    @Test
    void connectReturnsValidConnection() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn("sk-mistral");
        when(params.getTotalTimeout()).thenReturn(12345L);
        MistralAIModelConnection conn = (MistralAIModelConnection) provider.connect();
        assertNotNull(conn);
        assertEquals("sk-mistral", conn.getApiKey());
        assertEquals(12345L, conn.getTimeout());
    }

    @Test
    void connectWithNullApiKey() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn(null);
        when(params.getTotalTimeout()).thenReturn(12345L);
        MistralAIModelConnection conn = (MistralAIModelConnection) provider.connect();
        assertNotNull(conn);
        assertNull(conn.getApiKey());
    }

    @Test
    void connectWithZeroTimeout() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn("sk-mistral");
        when(params.getTotalTimeout()).thenReturn(0L);
        MistralAIModelConnection conn = (MistralAIModelConnection) provider.connect();
        assertNotNull(conn);
        assertEquals(0L, conn.getTimeout());
    }
} 
