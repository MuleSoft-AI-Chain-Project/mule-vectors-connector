package org.mule.extension.vectors.internal.connection.embeddings.nomic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnectionProvider;
import org.mule.runtime.http.api.client.HttpClient;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NomicModelConnectionProviderTest {

    NomicModelConnectionProvider provider;
    NomicModelConnectionParameters params;
    HttpClient httpClient;

    @BeforeEach
    void setUp() throws Exception {
        provider = new NomicModelConnectionProvider();
        params = mock(NomicModelConnectionParameters.class);
        httpClient = mock(HttpClient.class);
        // Inject httpClient into provider if needed (skip if not present)
        // Inject params into provider
        Field paramsField = NomicModelConnectionProvider.class.getDeclaredField("nomicModelConnectionParameters");
        paramsField.setAccessible(true);
        paramsField.set(provider, params);
    }

    @Test
    void connectReturnsValidConnection() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn("sk-nomic");
        when(params.getTotalTimeout()).thenReturn(12345L);
        NomicModelConnection conn = (NomicModelConnection) provider.connect();
        assertNotNull(conn);
        assertEquals("sk-nomic", conn.getApiKey());
        assertEquals(12345L, conn.getTimeout());
    }

    @Test
    void connectWithNullApiKey() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn(null);
        when(params.getTotalTimeout()).thenReturn(12345L);
        NomicModelConnection conn = (NomicModelConnection) provider.connect();
        assertNull(conn.getApiKey());
    }

    @Test
    void connectWithZeroTimeout() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn("sk-nomic");
        when(params.getTotalTimeout()).thenReturn(0L);
        NomicModelConnection conn = (NomicModelConnection) provider.connect();
        assertEquals(0L, conn.getTimeout());
    }
} 
