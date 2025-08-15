package org.mule.extension.vectors.internal.connection.provider.embeddings.azureopenai;

import static org.junit.jupiter.api.Assertions.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.azureopenai.AzureOpenAIModelConnectionParameters;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class AzureOpenAIModelConnectionParametersTest {

  @Test
  void testGettersReturnSetValues() throws Exception {
    AzureOpenAIModelConnectionParameters params = new AzureOpenAIModelConnectionParameters();
    // Set private fields via reflection
    Field apiKeyField = AzureOpenAIModelConnectionParameters.class.getDeclaredField("apiKey");
    apiKeyField.setAccessible(true);
    apiKeyField.set(params, "sk-azure");
    Field endpointField = AzureOpenAIModelConnectionParameters.class.getDeclaredField("endpoint");
    endpointField.setAccessible(true);
    endpointField.set(params, "https://azure.openai");
    Field timeoutField = AzureOpenAIModelConnectionParameters.class.getField("totalTimeout");
    timeoutField.setAccessible(true);
    timeoutField.set(params, 12345L);
    assertEquals("sk-azure", params.getApiKey());
    assertEquals("https://azure.openai", params.getEndpoint());
    assertEquals(12345L, params.getTimeout());
  }

  @Test
  void testDefaultTimeoutIsZeroInPlainJava() {
    AzureOpenAIModelConnectionParameters params = new AzureOpenAIModelConnectionParameters();
    // In plain Java, default is 0; Mule injects @Optional default at runtime
    assertEquals(0L, params.getTimeout());
  }
}
