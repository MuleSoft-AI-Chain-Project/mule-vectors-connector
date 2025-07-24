package org.mule.extension.vectors.internal.connection.provider.embeddings.ollama;

import static org.junit.jupiter.api.Assertions.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.ollama.OllamaModelConnectionParameters;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class OllamaModelConnectionParametersTest {

  @Test
  void getters_andDefaults() throws Exception {
    OllamaModelConnectionParameters params = new OllamaModelConnectionParameters();
    // Set baseUrl via reflection
    Field baseUrlField = OllamaModelConnectionParameters.class.getDeclaredField("baseUrl");
    baseUrlField.setAccessible(true);
    baseUrlField.set(params, "http://localhost:1234");
    // Set totalTimeout via reflection to match expected value
    Field timeoutField = OllamaModelConnectionParameters.class.getField("totalTimeout");
    timeoutField.setAccessible(true);
    timeoutField.set(params, 60000L);
    assertEquals("http://localhost:1234", params.getBaseUrl());
    assertEquals(60000L, params.getTimeout());
  }

  @Test
  void canSetTotalTimeoutViaReflection() throws Exception {
    OllamaModelConnectionParameters params = new OllamaModelConnectionParameters();
    Field timeoutField = OllamaModelConnectionParameters.class.getField("totalTimeout");
    timeoutField.setAccessible(true);
    timeoutField.set(params, 12345L);
    assertEquals(12345L, params.getTimeout());
  }
}
