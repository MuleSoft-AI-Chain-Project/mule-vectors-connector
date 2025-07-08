package org.mule.extension.vectors.internal.model.azureaivision;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AzureAIVisionServiceProviderTest {

    @Test
    void getBuilder_returnsAzureAIVisionBuilderWithCorrectParams() throws Exception {
        AzureAIVisionServiceProvider provider = new AzureAIVisionServiceProvider();
        AzureAIVisionModelConnection modelConnection = mock(AzureAIVisionModelConnection.class);
        EmbeddingModelParameters modelParameters = mock(EmbeddingModelParameters.class);

        EmbeddingServiceBuilder builder = provider.getBuilder(modelConnection, modelParameters);
        assertThat(builder).isInstanceOf(AzureAIVisionBuilder.class);
        AzureAIVisionBuilder azureBuilder = (AzureAIVisionBuilder) builder;
        // Use reflection to check private fields
        var paramsField = AzureAIVisionBuilder.class.getDeclaredField("embeddingModelParameters");
        paramsField.setAccessible(true);
        var connField = AzureAIVisionBuilder.class.getDeclaredField("azureAIVisionModelConnection");
        connField.setAccessible(true);
        assertThat(paramsField.get(azureBuilder)).isEqualTo(modelParameters);
        assertThat(connField.get(azureBuilder)).isEqualTo(modelConnection);
    }

    @Test
    void getBuilder_throwsClassCastExceptionOnInvalidConnectionType() {
        AzureAIVisionServiceProvider provider = new AzureAIVisionServiceProvider();
        BaseModelConnection wrongConnection = mock(BaseModelConnection.class); // not AzureAIVisionModelConnection
        EmbeddingModelParameters modelParameters = mock(EmbeddingModelParameters.class);
        assertThatThrownBy(() -> provider.getBuilder(wrongConnection, modelParameters))
                .isInstanceOf(ClassCastException.class);
    }
} 