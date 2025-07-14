package org.mule.extension.vectors.internal.model.azureaivision;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.embeddings.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.azureaivision.AzureAIVisionBuilder;
import org.mule.extension.vectors.internal.service.embeddings.azureaivision.AzureAIVisionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AzureAIVisionBuilderTest {

    @Test
    void builder_setsAllFieldsCorrectly() {
        AzureAIVisionModelConnection modelConnection = mock(AzureAIVisionModelConnection.class);
        EmbeddingModelParameters modelParameters = mock(EmbeddingModelParameters.class);
        int dimensions = 128;

        AzureAIVisionBuilder builder = new AzureAIVisionBuilder()
                .modelConnections(modelConnection)
                .modelParameters(modelParameters)
                .modelDimensions(dimensions);

        // Use reflection to check private fields
        try {
            var connField = AzureAIVisionBuilder.class.getDeclaredField("azureAIVisionModelConnection");
            connField.setAccessible(true);
            var paramsField = AzureAIVisionBuilder.class.getDeclaredField("embeddingModelParameters");
            paramsField.setAccessible(true);
            var dimField = AzureAIVisionBuilder.class.getDeclaredField("dimensions");
            dimField.setAccessible(true);
            assertThat(connField.get(builder)).isEqualTo(modelConnection);
            assertThat(paramsField.get(builder)).isEqualTo(modelParameters);
            assertThat(dimField.get(builder)).isEqualTo(dimensions);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void build_returnsAzureAIVisionServiceWithCorrectFields() {
        AzureAIVisionModelConnection modelConnection = mock(AzureAIVisionModelConnection.class);
        EmbeddingModelParameters modelParameters = mock(EmbeddingModelParameters.class);
        int dimensions = 256;

        AzureAIVisionBuilder builder = new AzureAIVisionBuilder()
                .modelConnections(modelConnection)
                .modelParameters(modelParameters)
                .modelDimensions(dimensions);

        EmbeddingService service = builder.build();
        assertThat(service).isInstanceOf(AzureAIVisionService.class);
        AzureAIVisionService azureService = (AzureAIVisionService) service;
        // Use reflection to check private fields
        try {
            var connField = AzureAIVisionService.class.getDeclaredField("azureAIVisionModelConnection");
            connField.setAccessible(true);
            var paramsField = AzureAIVisionService.class.getDeclaredField("embeddingModelParameters");
            paramsField.setAccessible(true);
            var dimField = AzureAIVisionService.class.getDeclaredField("dimensions");
            dimField.setAccessible(true);
            assertThat(connField.get(azureService)).isEqualTo(modelConnection);
            assertThat(paramsField.get(azureService)).isEqualTo(modelParameters);
            assertThat(dimField.get(azureService)).isEqualTo(dimensions);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void build_withMissingFields_stillBuildsButFieldsAreNull() {
        AzureAIVisionBuilder builder = new AzureAIVisionBuilder();
        EmbeddingService service = builder.build();
        assertThat(service).isInstanceOf(AzureAIVisionService.class);
        AzureAIVisionService azureService = (AzureAIVisionService) service;
        // Use reflection to check private fields
        try {
            var connField = AzureAIVisionService.class.getDeclaredField("azureAIVisionModelConnection");
            connField.setAccessible(true);
            var paramsField = AzureAIVisionService.class.getDeclaredField("embeddingModelParameters");
            paramsField.setAccessible(true);
            var dimField = AzureAIVisionService.class.getDeclaredField("dimensions");
            dimField.setAccessible(true);
            assertThat(connField.get(azureService)).isNull();
            assertThat(paramsField.get(azureService)).isNull();
            assertThat(dimField.get(azureService)).isNull();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
} 
