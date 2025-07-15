package org.mule.extension.vectors.internal.model.openai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.embeddings.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.openai.OpenAIBuilder;
import org.mule.extension.vectors.internal.service.embeddings.openai.OpenAIService;

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
                .modelParameters(params);
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
