package org.mule.extension.vectors.internal.model.vertexai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VertexAIBuilderTest {
    @Test
    void builder_setsAllFields_andBuildsService() {
        VertexAIModelConnection connection = mock(VertexAIModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        int dims = 42;
        VertexAIBuilder builder = new VertexAIBuilder()
                .modelConnections(connection)
                .modelParameters(params)
                .modelDimensions(dims);
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