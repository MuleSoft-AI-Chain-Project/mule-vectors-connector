package org.mule.extension.vectors.internal.model.huggingface;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HuggingFaceServiceProviderTest {
    @Test
    void getBuilder_returnsNonNull_andTypeSafe() {
        HuggingFaceServiceProvider provider = new HuggingFaceServiceProvider();
        HuggingFaceModelConnection connection = mock(HuggingFaceModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);

        EmbeddingServiceBuilder builder = provider.getBuilder(connection, params);
        assertNotNull(builder);
        assertTrue(builder instanceof HuggingFaceBuilder);
        // Should build a HuggingFaceService
        assertTrue(builder.build() instanceof HuggingFaceService);
    }

    @Test
    void getBuilder_withNulls_doesNotThrow() {
        HuggingFaceServiceProvider provider = new HuggingFaceServiceProvider();
        // Accepts nulls, but builder will NPE on build (acceptable for provider pattern)
        assertDoesNotThrow(() -> provider.getBuilder(null, null));
    }

    @Test
    void getBuilder_wrongType_throwsClassCastException() {
        HuggingFaceServiceProvider provider = new HuggingFaceServiceProvider();
        BaseModelConnection wrongConn = mock(BaseModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        // Should throw ClassCastException
        assertThrows(ClassCastException.class, () -> provider.getBuilder(wrongConn, params));
    }
} 