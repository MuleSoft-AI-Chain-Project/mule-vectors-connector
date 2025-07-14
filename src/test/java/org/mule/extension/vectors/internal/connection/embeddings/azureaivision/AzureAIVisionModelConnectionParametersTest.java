package org.mule.extension.vectors.internal.connection.embeddings.azureaivision;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class AzureAIVisionModelConnectionParametersTest {
    @Test
    void getters_andDefaults() throws Exception {
        AzureAIVisionModelConnectionParameters params = new AzureAIVisionModelConnectionParameters();
        // Default value for apiVersion
        assertEquals("2023-04-01-preview", params.getApiVersion());
        // Set endpoint via reflection
        Field endpointField = AzureAIVisionModelConnectionParameters.class.getDeclaredField("endpoint");
        endpointField.setAccessible(true);
        endpointField.set(params, "https://endpoint");
        assertEquals("https://endpoint", params.getEndpoint());
        // Set apiKey via reflection
        Field apiKeyField = AzureAIVisionModelConnectionParameters.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(params, "key");
        assertEquals("key", params.getApiKey());
        // Set totalTimeout via reflection
        Field timeoutField = AzureAIVisionModelConnectionParameters.class.getDeclaredField("totalTimeout");
        timeoutField.setAccessible(true);
        timeoutField.set(params, 60000L);
        assertEquals(60000L, params.getTotalTimeout());
    }

    @Test
    void canSetApiVersionViaReflection() throws Exception {
        AzureAIVisionModelConnectionParameters params = new AzureAIVisionModelConnectionParameters();
        Field apiVersionField = AzureAIVisionModelConnectionParameters.class.getDeclaredField("apiVersion");
        apiVersionField.setAccessible(true);
        apiVersionField.set(params, "2024-01-01");
        assertEquals("2024-01-01", params.getApiVersion());
    }
} 
