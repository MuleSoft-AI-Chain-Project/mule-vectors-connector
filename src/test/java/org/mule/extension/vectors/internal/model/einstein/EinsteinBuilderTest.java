package org.mule.extension.vectors.internal.model.einstein;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.embeddings.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.einstein.EinsteinBuilder;
import org.mule.extension.vectors.internal.service.embeddings.einstein.EinsteinService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EinsteinBuilderTest {
    @Test
    void builder_setsAllFields_andBuildsService() {
        EinsteinModelConnection connection = mock(EinsteinModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        int dims = 123;
        EinsteinBuilder builder = new EinsteinBuilder()
                .modelConnections(connection)
                .modelParameters(params)
                .modelDimensions(dims);
        EmbeddingService service = builder.build();
        assertNotNull(service);
        assertTrue(service instanceof EinsteinService);
    }

    @Test
    void builder_handlesNulls() {
        EinsteinBuilder builder = new EinsteinBuilder();
        assertDoesNotThrow(() -> builder.modelConnections(null));
        assertDoesNotThrow(() -> builder.modelParameters(null));
        assertDoesNotThrow(() -> builder.modelDimensions(null));
        // build() with all nulls should still return a non-null EinsteinService
        EmbeddingService service = builder.build();
        assertNotNull(service);
        assertTrue(service instanceof EinsteinService);
    }
} 
