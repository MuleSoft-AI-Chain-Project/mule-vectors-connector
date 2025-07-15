package org.mule.extension.vectors.internal.model.azureopenai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.embeddings.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.azureopenai.AzureOpenAIBuilder;
import org.mule.extension.vectors.internal.service.embeddings.azureopenai.AzureOpenAIService;

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
                .modelParameters(params);
        EmbeddingService service = builder.build();
        assertNotNull(service);
        assertTrue(service instanceof AzureOpenAIService);
    }

    @Test
    void builder_handlesNulls() {
        AzureOpenAIBuilder builder = new AzureOpenAIBuilder();
        assertDoesNotThrow(() -> builder.modelConnections(null));
        assertDoesNotThrow(() -> builder.modelParameters(null));
    }
} 
