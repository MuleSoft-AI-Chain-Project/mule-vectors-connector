package org.mule.extension.vectors.internal.connection.provider.embeddings.ollama;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.ollama.OllamaModelConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.embeddings.ollama.OllamaModelConnectionProvider;
import org.mule.runtime.http.api.client.HttpClient;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class OllamaModelConnectionProviderTest {

  @Test
  void connect_returnsValidConnection() throws Exception {
    OllamaModelConnectionProvider provider = spy(new OllamaModelConnectionProvider());
    OllamaModelConnectionParameters params = new OllamaModelConnectionParameters();
    Field baseUrlField = OllamaModelConnectionParameters.class.getDeclaredField("baseUrl");
    baseUrlField.setAccessible(true);
    baseUrlField.set(params, "http://localhost:1234");
    Field timeoutField = OllamaModelConnectionParameters.class.getField("totalTimeout");
    timeoutField.setAccessible(true);
    timeoutField.set(params, 1234L);
    Field groupField = OllamaModelConnectionProvider.class.getDeclaredField("ollamaModelConnectionParameters");
    groupField.setAccessible(true);
    groupField.set(provider, params);
    doReturn(mock(HttpClient.class)).when(provider).getHttpClient();
    BaseModelConnection conn = provider.connect();
    assertTrue(conn instanceof OllamaModelConnection);
    OllamaModelConnection ollamaConn = (OllamaModelConnection) conn;
    assertEquals("http://localhost:1234", ollamaConn.getBaseUrl());
    assertEquals(1234L, ollamaConn.getTimeout());
  }
}
