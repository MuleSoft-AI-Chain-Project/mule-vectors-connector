package org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface;

import static org.junit.jupiter.api.Assertions.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface.HuggingFaceModelConnectionParameters;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class HuggingFaceModelConnectionParametersTest {

  @Test
  void testGettersReturnSetValues() throws Exception {
    HuggingFaceModelConnectionParameters params = new HuggingFaceModelConnectionParameters();
    // Set private fields via reflection
    Field apiKeyField = HuggingFaceModelConnectionParameters.class.getDeclaredField("apiKey");
    apiKeyField.setAccessible(true);
    apiKeyField.set(params, "sk-hf");
    Field timeoutField = HuggingFaceModelConnectionParameters.class.getField("totalTimeout");
    timeoutField.setAccessible(true);
    timeoutField.set(params, 12345L);
    assertEquals("sk-hf", params.getApiKey());
    assertEquals(12345L, params.getTimeout());
  }

  @Test
  void testDefaultTimeoutIsZeroInPlainJava() {
    HuggingFaceModelConnectionParameters params = new HuggingFaceModelConnectionParameters();
    // In plain Java, default is 0; Mule injects @Optional default at runtime
    assertEquals(0L, params.getTimeout());
  }
}
