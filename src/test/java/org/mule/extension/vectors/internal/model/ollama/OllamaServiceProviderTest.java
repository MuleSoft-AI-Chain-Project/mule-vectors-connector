package org.mule.extension.vectors.internal.model.ollama;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OllamaServiceProviderTest {
    @Test
    void getBuilder_returnsOllamaBuilder() {
        OllamaServiceProvider provider = new OllamaServiceProvider();
        OllamaModelConnection conn = mock(OllamaModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        EmbeddingServiceBuilder builder = provider.getBuilder(conn, params);
        assertNotNull(builder);
        assertTrue(builder instanceof OllamaBuilder);
        assertNotNull(builder.build());
        assertTrue(builder.build() instanceof OllamaService);
    }

    @Test
    void getBuilder_withNulls() {
        OllamaServiceProvider provider = new OllamaServiceProvider();
        EmbeddingServiceBuilder builder = provider.getBuilder(null, null);
        assertNotNull(builder);
        assertTrue(builder instanceof OllamaBuilder);
        assertNotNull(builder.build());
    }

    @Test
    void getBuilder_typeSafety() {
        OllamaServiceProvider provider = new OllamaServiceProvider();
        BaseModelConnection baseConn = mock(BaseModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        // Should throw ClassCastException if not OllamaModelConnection
        assertThrows(ClassCastException.class, () -> provider.getBuilder(baseConn, params));
    }
} 