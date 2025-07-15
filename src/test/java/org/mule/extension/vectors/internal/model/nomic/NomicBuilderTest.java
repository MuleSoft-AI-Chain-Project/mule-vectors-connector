package org.mule.extension.vectors.internal.model.nomic;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.embeddings.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.nomic.NomicBuilder;
import org.mule.extension.vectors.internal.service.embeddings.nomic.NomicService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NomicBuilderTest {
    @Test
    void builder_setsAllFields_andBuildsService() {
        NomicModelConnection connection = mock(NomicModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        int dims = 42;
        NomicBuilder builder = new NomicBuilder()
                .modelConnections(connection)
                .modelParameters(params);
        EmbeddingService service = builder.build();
        assertNotNull(service);
        assertTrue(service instanceof NomicService);
    }

    @Test
    void builder_handlesNulls() {
        NomicBuilder builder = new NomicBuilder();
        // Should not throw on build with nulls, but service may not work
        EmbeddingService service = builder.build();
        assertNotNull(service);
        assertTrue(service instanceof NomicService);
    }
} 
