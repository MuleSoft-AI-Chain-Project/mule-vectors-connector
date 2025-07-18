package org.mule.extension.vectors.internal.connection.provider.embeddings.mistralai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.embeddings.mistralai.MistralAIModelConnectionParameters;

import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class MistralAIModelConnectionParametersTest {

    @Test
    void testGettersReturnSetValues() throws Exception {
        MistralAIModelConnectionParameters params = new MistralAIModelConnectionParameters();
        // Set private fields via reflection
        Field apiKeyField = MistralAIModelConnectionParameters.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(params, "sk-mistral");
        Field timeoutField = MistralAIModelConnectionParameters.class.getField("totalTimeout");
        timeoutField.setAccessible(true);
        timeoutField.set(params, 12345L);
        assertEquals("sk-mistral", params.getApiKey());
        assertEquals(12345L, params.getTimeout());
    }

    @Test
    void testDefaultTimeout() {
        MistralAIModelConnectionParameters params = new MistralAIModelConnectionParameters();
        // In plain Java, default is 0, not the @Optional value
        assertEquals(0L, params.getTimeout());
    }
} 
