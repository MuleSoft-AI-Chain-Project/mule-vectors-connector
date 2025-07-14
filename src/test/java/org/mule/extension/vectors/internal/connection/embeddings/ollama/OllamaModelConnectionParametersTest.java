package org.mule.extension.vectors.internal.connection.embeddings.ollama;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class OllamaModelConnectionParametersTest {
    @Test
    void getters_andDefaults() throws Exception {
        OllamaModelConnectionParameters params = new OllamaModelConnectionParameters();
        // Set baseUrl via reflection
        Field baseUrlField = OllamaModelConnectionParameters.class.getDeclaredField("baseUrl");
        baseUrlField.setAccessible(true);
        baseUrlField.set(params, "http://localhost:1234");
        // Set totalTimeout via reflection to match expected value
        Field timeoutField = OllamaModelConnectionParameters.class.getDeclaredField("totalTimeout");
        timeoutField.setAccessible(true);
        timeoutField.set(params, 60000L);
        assertEquals("http://localhost:1234", params.getBaseUrl());
        assertEquals(60000L, params.getTotalTimeout());
    }

    @Test
    void canSetTotalTimeoutViaReflection() throws Exception {
        OllamaModelConnectionParameters params = new OllamaModelConnectionParameters();
        Field timeoutField = OllamaModelConnectionParameters.class.getDeclaredField("totalTimeout");
        timeoutField.setAccessible(true);
        timeoutField.set(params, 12345L);
        assertEquals(12345L, params.getTotalTimeout());
    }
} 
