package org.mule.extension.vectors.internal.model.ollama;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.ollama.OllamaBuilder;
import org.mule.extension.vectors.internal.service.embeddings.ollama.OllamaService;

import org.junit.jupiter.api.Test;

class OllamaBuilderTest {

  @Test
  void builder_setsAllFields_andBuildsService() {
    OllamaModelConnection connection = mock(OllamaModelConnection.class);
    EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
    int dims = 42;
    OllamaBuilder builder = new OllamaBuilder()
        .modelConnections(connection)
        .modelParameters(params);
    EmbeddingService service = builder.build();
    assertNotNull(service);
    assertTrue(service instanceof OllamaService);
  }

  @Test
  void builder_handlesNulls() {
    OllamaBuilder builder = new OllamaBuilder();
    // Should not throw on build with nulls, but service may not work
    EmbeddingService service = builder.build();
    assertNotNull(service);
    assertTrue(service instanceof OllamaService);
  }
}
