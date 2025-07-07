package org.mule.extension.vectors.internal.connection.model.vertexai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnectionParameters;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnectionProvider;
import org.mule.runtime.http.api.client.HttpClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VertexAIModelConnectionProviderTest {

    VertexAIModelConnectionProvider provider;
    VertexAIModelConnectionParameters params;
    HttpClient httpClient;

    @BeforeEach
    void setUp() throws Exception {
        provider = new VertexAIModelConnectionProvider();
        params = mock(VertexAIModelConnectionParameters.class);
        // Only inject params into provider
        java.lang.reflect.Field paramsField = VertexAIModelConnectionProvider.class.getDeclaredField("vertexAIModelConnectionParameters");
        paramsField.setAccessible(true);
        paramsField.set(provider, params);
    }

    @Test
    void connectReturnsValidConnection() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getProjectId()).thenReturn("test-project");
        when(params.getLocation()).thenReturn("us-central1");
        VertexAIModelConnection conn = (VertexAIModelConnection) provider.connect();
        assertNotNull(conn);
        assertEquals("test-project", conn.getProjectId());
        assertEquals("us-central1", conn.getLocation());
    }

    @Test
    void connectWithNullProjectId() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getProjectId()).thenReturn(null);
        when(params.getLocation()).thenReturn("us-central1");
        VertexAIModelConnection conn = (VertexAIModelConnection) provider.connect();
        assertNull(conn.getProjectId());
        assertEquals("us-central1", conn.getLocation());
    }

    @Test
    void connectWithNullLocation() throws org.mule.runtime.api.connection.ConnectionException {
        when(params.getProjectId()).thenReturn("test-project");
        when(params.getLocation()).thenReturn(null);
        VertexAIModelConnection conn = (VertexAIModelConnection) provider.connect();
        assertEquals("test-project", conn.getProjectId());
        assertNull(conn.getLocation());
    }
} 
