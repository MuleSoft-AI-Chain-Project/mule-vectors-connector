package org.mule.extension.vectors.internal.connection.embeddings.azureopenai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.connection.provider.azureopenai.AzureOpenAIModelConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.azureopenai.AzureOpenAIModelConnectionProvider;
import org.mule.runtime.http.api.client.HttpClient;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureOpenAIModelConnectionProviderTest {

    AzureOpenAIModelConnectionProvider provider;
    AzureOpenAIModelConnectionParameters params;
    HttpClient httpClient;

    @BeforeEach
    void setUp() throws Exception {
        provider = new AzureOpenAIModelConnectionProvider();
        params = mock(AzureOpenAIModelConnectionParameters.class);
        httpClient = mock(HttpClient.class);
        // Inject params into provider
        Field paramsField = AzureOpenAIModelConnectionProvider.class.getDeclaredField("azureOpenAIModelConnectionParameters");
        paramsField.setAccessible(true);
        paramsField.set(provider, params);
    }

    @Test
    void connectReturnsValidConnection() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn("sk-azure");
        when(params.getEndpoint()).thenReturn("https://azure.openai");
        when(params.getTotalTimeout()).thenReturn(12345L);
        AzureOpenAIModelConnection conn = (AzureOpenAIModelConnection) provider.connect();
        assertNotNull(conn);
        assertEquals("sk-azure", conn.getApiKey());
        assertEquals("https://azure.openai", conn.getEndpoint());
        assertEquals(12345L, conn.getTimeout());
    }

    @Test
    void connectWithNullApiKey() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn(null);
        when(params.getEndpoint()).thenReturn("https://azure.openai");
        when(params.getTotalTimeout()).thenReturn(12345L);
        AzureOpenAIModelConnection conn = (AzureOpenAIModelConnection) provider.connect();
        assertNull(conn.getApiKey());
    }

    @Test
    void connectWithNullEndpoint() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn("sk-azure");
        when(params.getEndpoint()).thenReturn(null);
        when(params.getTotalTimeout()).thenReturn(12345L);
        AzureOpenAIModelConnection conn = (AzureOpenAIModelConnection) provider.connect();
        assertNull(conn.getEndpoint());
    }

    @Test
    void connectWithZeroTimeout() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getApiKey()).thenReturn("sk-azure");
        when(params.getEndpoint()).thenReturn("https://azure.openai");
        when(params.getTotalTimeout()).thenReturn(0L);
        AzureOpenAIModelConnection conn = (AzureOpenAIModelConnection) provider.connect();
        assertEquals(0L, conn.getTimeout());
    }
} 
