package org.mule.extension.vectors.internal.model.azureopenai;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureopenAIServiceProviderTest {
    @Test
    void getBuilder_returnsAzureOpenAIBuilder() {
        AzureopenAIServiceProvider provider = new AzureopenAIServiceProvider();
        AzureOpenAIModelConnection connection = mock(AzureOpenAIModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        EmbeddingServiceBuilder builder = provider.getBuilder(connection, params);
        assertNotNull(builder);
        assertTrue(builder instanceof AzureOpenAIBuilder);
        assertNotNull(builder.build());
        assertTrue(builder.build() instanceof AzureOpenAIService);
    }

    @Test
    void getBuilder_withNulls_doesNotThrow() {
        AzureopenAIServiceProvider provider = new AzureopenAIServiceProvider();
        assertDoesNotThrow(() -> provider.getBuilder(null, null));
    }

    @Test
    void getBuilder_typeSafety() {
        AzureopenAIServiceProvider provider = new AzureopenAIServiceProvider();
        BaseModelConnection wrongType = mock(BaseModelConnection.class);
        EmbeddingModelParameters params = mock(EmbeddingModelParameters.class);
        assertThrows(ClassCastException.class, () -> provider.getBuilder(wrongType, params));
    }
} 