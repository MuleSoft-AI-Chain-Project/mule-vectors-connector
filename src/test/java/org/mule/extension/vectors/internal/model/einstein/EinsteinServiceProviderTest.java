package org.mule.extension.vectors.internal.model.einstein;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EinsteinServiceProviderTest {
    @Test
    void getBuilder_returnsEinsteinBuilder() {
        EinsteinServiceProvider provider = new EinsteinServiceProvider();
        EinsteinModelConnection conn = mock(EinsteinModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        EmbeddingServiceBuilder builder = provider.getBuilder(conn, params);
        assertNotNull(builder);
        assertTrue(builder instanceof EinsteinBuilder);
        assertNotNull(builder.build());
        assertTrue(builder.build() instanceof EinsteinService);
    }

    @Test
    void getBuilder_withNulls() {
        EinsteinServiceProvider provider = new EinsteinServiceProvider();
        assertDoesNotThrow(() -> provider.getBuilder(null, null));
    }

    @Test
    void getBuilder_typeSafety() {
        EinsteinServiceProvider provider = new EinsteinServiceProvider();
        BaseModelConnection wrongConn = mock(BaseModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        assertThrows(ClassCastException.class, () -> provider.getBuilder(wrongConn, params));
    }
} 