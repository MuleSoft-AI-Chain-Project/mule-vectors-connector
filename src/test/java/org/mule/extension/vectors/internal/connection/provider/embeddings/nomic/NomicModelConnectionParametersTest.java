package org.mule.extension.vectors.internal.connection.provider.embeddings.nomic;

import static org.junit.jupiter.api.Assertions.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnectionParameters;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class NomicModelConnectionParametersTest {

  @Test
  void testGettersReturnSetValues() throws Exception {
    NomicModelConnectionParameters params = new NomicModelConnectionParameters();
    // Set private fields via reflection
    Field apiKeyField = NomicModelConnectionParameters.class.getDeclaredField("apiKey");
    apiKeyField.setAccessible(true);
    apiKeyField.set(params, "sk-nomic");
    Field timeoutField = NomicModelConnectionParameters.class.getField("totalTimeout");
    timeoutField.setAccessible(true);
    timeoutField.set(params, 12345L);
    assertEquals("sk-nomic", params.getApiKey());
    assertEquals(12345L, params.getTimeout());
  }

  @Test
  void testDefaultTimeoutIsZeroInPlainJava() {
    NomicModelConnectionParameters params = new NomicModelConnectionParameters();
    // In plain Java, default is 0; Mule injects @Optional default at runtime
    assertEquals(0L, params.getTimeout());
  }
}
