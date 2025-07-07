package org.mule.extension.vectors.internal.connection.model.huggingface;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnectionParameters;
import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnectionProvider;
import org.mule.runtime.http.api.client.HttpClient;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HuggingFaceModelConnectionProviderTest {

    HuggingFaceModelConnectionProvider provider;
    HuggingFaceModelConnectionParameters params;
    HttpClient httpClient;

    @BeforeEach
    void setUp() throws Exception {
        provider = new HuggingFaceModelConnectionProvider();
        params = mock(HuggingFaceModelConnectionParameters.class);
        httpClient = mock(HttpClient.class);
        // Inject params into provider
        Field paramsField = HuggingFaceModelConnectionProvider.class.getDeclaredField("huggingFaceModelConnectionParameters");
        paramsField.setAccessible(true);
        paramsField.set(provider, params);
    }

    @Test
    void connectReturnsValidConnection() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn("sk-hf");
        when(params.getTotalTimeout()).thenReturn(12345L);
        HuggingFaceModelConnection conn = (HuggingFaceModelConnection) provider.connect();
        assertNotNull(conn);
        assertEquals("sk-hf", conn.getApiKey());
        assertEquals(12345L, conn.getTimeout());
    }

    @Test
    void connectWithNullApiKey() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn(null);
        when(params.getTotalTimeout()).thenReturn(12345L);
        HuggingFaceModelConnection conn = (HuggingFaceModelConnection) provider.connect();
        assertNull(conn.getApiKey());
    }

    @Test
    void connectWithZeroTimeout() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn("sk-hf");
        when(params.getTotalTimeout()).thenReturn(0L);
        HuggingFaceModelConnection conn = (HuggingFaceModelConnection) provider.connect();
        assertEquals(0L, conn.getTimeout());
    }
} 
