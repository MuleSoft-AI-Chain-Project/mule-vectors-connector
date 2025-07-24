package org.mule.extension.vectors.internal.model.vertexai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.embeddings.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.vertexai.VertexAIBuilder;
import org.mule.extension.vectors.internal.service.embeddings.vertexai.VertexAIService;

import org.junit.jupiter.api.Test;

class VertexAIBuilderTest {

  @Test
  void builder_setsAllFields_andBuildsService() {
    VertexAIModelConnection connection = mock(VertexAIModelConnection.class);
    EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
    int dims = 42;
    VertexAIBuilder builder = new VertexAIBuilder()
        .modelConnections(connection)
        .modelParameters(params);
    EmbeddingService service = builder.build();
    assertNotNull(service);
    assertTrue(service instanceof VertexAIService);
  }

  @Test
  void builder_handlesNulls() {
    VertexAIBuilder builder = new VertexAIBuilder();
    // Should not throw on build with nulls, but service may not work
    EmbeddingService service = builder.build();
    assertNotNull(service);
    assertTrue(service instanceof VertexAIService);
  }
}
