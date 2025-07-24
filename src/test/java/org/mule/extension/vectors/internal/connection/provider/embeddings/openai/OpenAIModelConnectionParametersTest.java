package org.mule.extension.vectors.internal.connection.provider.embeddings.openai;

import static org.junit.jupiter.api.Assertions.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.openai.OpenAIModelConnectionParameters;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class OpenAIModelConnectionParametersTest {

  @Test
  void testGettersReturnSetValues() throws Exception {
    OpenAIModelConnectionParameters params = new OpenAIModelConnectionParameters();
    // Set private fields via reflection
    Field apiKeyField = OpenAIModelConnectionParameters.class.getDeclaredField("apiKey");
    apiKeyField.setAccessible(true);
    apiKeyField.set(params, "sk-123");
    Field timeoutField = OpenAIModelConnectionParameters.class.getField("totalTimeout");
    timeoutField.setAccessible(true);
    timeoutField.set(params, 42000L);
    assertEquals("sk-123", params.getApiKey());
    assertEquals(42000L, params.getTimeout());
  }

  @Test
  void testDefaultTimeoutIsZeroInPlainJava() throws Exception {
    // Note: The @Optional(defaultValue = "60000") is enforced by Mule runtime, not by Java itself.
    // In plain Java, the default for long is 0.
    OpenAIModelConnectionParameters params = new OpenAIModelConnectionParameters();
    assertEquals(0L, params.getTimeout());
  }

  @Test
  void testImmutability() {
    // No setters, fields are private, only getters
    var methods = OpenAIModelConnectionParameters.class.getDeclaredMethods();
    for (var m : methods) {
      assertFalse(m.getName().startsWith("set"), "Should not have setters");
    }
  }
}
