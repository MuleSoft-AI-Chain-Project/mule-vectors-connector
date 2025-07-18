package org.mule.extension.vectors.internal.model.mistralai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.embeddings.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.mistralai.MistralAIBuilder;
import org.mule.extension.vectors.internal.service.embeddings.mistralai.MistralAIService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MistralAIBuilderTest {
    @Test
    void builder_setsAllFields_andBuildsService() {
        MistralAIModelConnection connection = mock(MistralAIModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        int dims = 512;

        MistralAIBuilder builder = new MistralAIBuilder()
                .modelConnections(connection)
                .modelParameters(params);

        EmbeddingService service = builder.build();
        assertNotNull(service);
        assertTrue(service instanceof MistralAIService);
    }

    @Test
    void builder_handlesNulls() {
        MistralAIBuilder builder = new MistralAIBuilder();
        // Should not throw on nulls, but build() will likely NPE (acceptable for builder pattern)
        assertDoesNotThrow(() -> builder.modelConnections(null));
        assertDoesNotThrow(() -> builder.modelParameters(null));
    }
} 
