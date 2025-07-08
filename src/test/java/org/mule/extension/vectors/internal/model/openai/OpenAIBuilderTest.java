package org.mule.extension.vectors.internal.model.openai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenAIBuilderTest {
    @Test
    void builder_setsAllFields_andBuildsService() {
        OpenAIModelConnection connection = mock(OpenAIModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        int dims = 42;
        OpenAIBuilder builder = new OpenAIBuilder()
                .modelConnections(connection)
                .modelParameters(params)
                .modelDimensions(dims);
        EmbeddingService service = builder.build();
        assertNotNull(service);
        assertTrue(service instanceof OpenAIService);
    }

    @Test
    void builder_handlesNulls() {
        OpenAIBuilder builder = new OpenAIBuilder();
        // Should not throw on build with nulls, but service may not work
        EmbeddingService service = builder.build();
        assertNotNull(service);
        assertTrue(service instanceof OpenAIService);
    }
} 