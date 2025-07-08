package org.mule.extension.vectors.internal.model.openai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpenAIServiceProviderTest {
    @Test
    void getBuilder_returnsOpenAIBuilder() {
        OpenAIServiceProvider provider = new OpenAIServiceProvider();
        OpenAIModelConnection conn = mock(OpenAIModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        EmbeddingServiceBuilder builder = provider.getBuilder(conn, params);
        assertNotNull(builder);
        assertTrue(builder instanceof OpenAIBuilder);
        assertNotNull(builder.build());
        assertTrue(builder.build() instanceof OpenAIService);
    }

    @Test
    void getBuilder_withNulls() {
        OpenAIServiceProvider provider = new OpenAIServiceProvider();
        EmbeddingServiceBuilder builder = provider.getBuilder(null, null);
        assertNotNull(builder);
        assertTrue(builder instanceof OpenAIBuilder);
        assertNotNull(builder.build());
    }

    @Test
    void getBuilder_typeSafety() {
        OpenAIServiceProvider provider = new OpenAIServiceProvider();
        BaseModelConnection baseConn = mock(BaseModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        // Should throw ClassCastException if not OpenAIModelConnection
        assertThrows(ClassCastException.class, () -> provider.getBuilder(baseConn, params));
    }
} 