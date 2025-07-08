package org.mule.extension.vectors.internal.model.vertexai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VertexAIServiceProviderTest {
    @Test
    void getBuilder_returnsVertexAIBuilder() {
        VertexAIServiceProvider provider = new VertexAIServiceProvider();
        VertexAIModelConnection conn = mock(VertexAIModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        EmbeddingServiceBuilder builder = provider.getBuilder(conn, params);
        assertNotNull(builder);
        assertTrue(builder instanceof VertexAIBuilder);
        assertNotNull(builder.build());
        assertTrue(builder.build() instanceof VertexAIService);
    }

    @Test
    void getBuilder_withNulls() {
        VertexAIServiceProvider provider = new VertexAIServiceProvider();
        EmbeddingServiceBuilder builder = provider.getBuilder(null, null);
        assertNotNull(builder);
        assertTrue(builder instanceof VertexAIBuilder);
        assertNotNull(builder.build());
    }

    @Test
    void getBuilder_typeSafety() {
        VertexAIServiceProvider provider = new VertexAIServiceProvider();
        BaseModelConnection baseConn = mock(BaseModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        // Should throw ClassCastException if not VertexAIModelConnection
        assertThrows(ClassCastException.class, () -> provider.getBuilder(baseConn, params));
    }
} 