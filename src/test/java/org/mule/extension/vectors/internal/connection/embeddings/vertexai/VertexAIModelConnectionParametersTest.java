package org.mule.extension.vectors.internal.connection.embeddings.vertexai;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class VertexAIModelConnectionParametersTest {

    @Test
    void testGettersReturnSetValues() throws Exception {
        VertexAIModelConnectionParameters params = new VertexAIModelConnectionParameters();
        // Set private fields via reflection
        Field projectIdField = VertexAIModelConnectionParameters.class.getDeclaredField("projectId");
        projectIdField.setAccessible(true);
        projectIdField.set(params, "test-project");
        Field locationField = VertexAIModelConnectionParameters.class.getDeclaredField("location");
        locationField.setAccessible(true);
        locationField.set(params, "us-central1");
        Field clientEmailField = VertexAIModelConnectionParameters.class.getDeclaredField("clientEmail");
        clientEmailField.setAccessible(true);
        clientEmailField.set(params, "test@email.com");
        Field clientIdField = VertexAIModelConnectionParameters.class.getDeclaredField("clientId");
        clientIdField.setAccessible(true);
        clientIdField.set(params, "client-123");
        Field privateKeyIdField = VertexAIModelConnectionParameters.class.getDeclaredField("privateKeyId");
        privateKeyIdField.setAccessible(true);
        privateKeyIdField.set(params, "key-456");
        assertEquals("test-project", params.getProjectId());
        assertEquals("us-central1", params.getLocation());
        assertEquals("test@email.com", params.getClientEmail());
        assertEquals("client-123", params.getClientId());
        assertEquals("key-456", params.getPrivateKeyId());
    }

    @Test
    void testDefaultValues() {
        VertexAIModelConnectionParameters params = new VertexAIModelConnectionParameters();
        // Default values for fields (should be null for objects)
        assertNull(params.getProjectId());
        assertNull(params.getLocation());
        assertNull(params.getClientEmail());
        assertNull(params.getClientId());
        assertNull(params.getPrivateKeyId());
    }

    @Test
    void testImmutability() {
        VertexAIModelConnectionParameters params = new VertexAIModelConnectionParameters();
        // No setters, fields are private
        assertThrows(NoSuchMethodException.class, () ->
            VertexAIModelConnectionParameters.class.getDeclaredMethod("setProjectId", String.class)
        );
    }
} 
