package org.mule.extension.vectors.internal.model.nomic;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NomicServiceProviderTest {
    @Test
    void getBuilder_returnsNomicBuilder() {
        NomicServiceProvider provider = new NomicServiceProvider();
        NomicModelConnection conn = mock(NomicModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        EmbeddingServiceBuilder builder = provider.getBuilder(conn, params);
        assertNotNull(builder);
        assertTrue(builder instanceof NomicBuilder);
        assertNotNull(builder.build());
        assertTrue(builder.build() instanceof NomicService);
    }

    @Test
    void getBuilder_withNulls() {
        NomicServiceProvider provider = new NomicServiceProvider();
        EmbeddingServiceBuilder builder = provider.getBuilder(null, null);
        assertNotNull(builder);
        assertTrue(builder instanceof NomicBuilder);
        assertNotNull(builder.build());
    }

    @Test
    void getBuilder_typeSafety() {
        NomicServiceProvider provider = new NomicServiceProvider();
        BaseModelConnection baseConn = mock(BaseModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        // Should throw ClassCastException if not NomicModelConnection
        assertThrows(ClassCastException.class, () -> provider.getBuilder(baseConn, params));
    }
} 