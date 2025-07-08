package org.mule.extension.vectors.internal.model.mistralai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MistralAIServiceProviderTest {
    @Test
    void getBuilder_returnsNonNull_andTypeSafe() {
        MistralAIServiceProvider provider = new MistralAIServiceProvider();
        MistralAIModelConnection connection = mock(MistralAIModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);

        EmbeddingServiceBuilder builder = provider.getBuilder(connection, params);
        assertNotNull(builder);
        assertTrue(builder instanceof MistralAIBuilder);
        // Should build a MistralAIService
        assertTrue(builder.build() instanceof MistralAIService);
    }

    @Test
    void getBuilder_withNulls_doesNotThrow() {
        MistralAIServiceProvider provider = new MistralAIServiceProvider();
        // Accepts nulls, but builder will NPE on build (acceptable for provider pattern)
        assertDoesNotThrow(() -> provider.getBuilder(null, null));
    }

    @Test
    void getBuilder_wrongType_throwsClassCastException() {
        MistralAIServiceProvider provider = new MistralAIServiceProvider();
        BaseModelConnection wrongConn = mock(BaseModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        // Should throw ClassCastException
        assertThrows(ClassCastException.class, () -> provider.getBuilder(wrongConn, params));
    }
} 