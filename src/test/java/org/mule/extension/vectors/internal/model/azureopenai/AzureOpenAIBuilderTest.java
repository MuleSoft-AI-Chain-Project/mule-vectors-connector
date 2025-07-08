package org.mule.extension.vectors.internal.model.azureopenai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureOpenAIBuilderTest {
    @Test
    void builder_setsAllFields_andBuildsService() {
        AzureOpenAIModelConnection connection = mock(AzureOpenAIModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        int dimensions = 1536;
        AzureOpenAIBuilder builder = new AzureOpenAIBuilder()
                .modelConnections(connection)
                .modelParameters(params)
                .modelDimensions(dimensions);
        EmbeddingService service = builder.build();
        assertNotNull(service);
        assertTrue(service instanceof AzureOpenAIService);
    }

    @Test
    void builder_handlesNulls() {
        AzureOpenAIBuilder builder = new AzureOpenAIBuilder();
        assertDoesNotThrow(() -> builder.modelConnections(null));
        assertDoesNotThrow(() -> builder.modelParameters(null));
        assertDoesNotThrow(() -> builder.modelDimensions(null));
    }
} 