package org.mule.extension.vectors.internal.service.embeddings.azureopenai;

import org.mule.extension.vectors.internal.connection.embeddings.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceBuilder;

/**
 * AzureOpenAIBuilder constructs AzureOpenAIService for embedding operations.
 *
 * <p>Preferred usage: Use via EmbeddingServiceFactoryBuilder.getBuilder(...) as per MDC rule
 * 'Direct Construction of EmbeddingServiceBuilder via EmbeddingServiceFactoryBuilder'.
 * This bypasses the provider indirection for clarity and maintainability.</p>
 */
public class AzureOpenAIBuilder implements EmbeddingServiceBuilder {
    private AzureOpenAIModelConnection azureOpenAIModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;


    public AzureOpenAIBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public AzureOpenAIBuilder modelConnections(AzureOpenAIModelConnection azureOpenAIModelConnection) {
      this.azureOpenAIModelConnection = azureOpenAIModelConnection;
      return this;
    }
    public AzureOpenAIBuilder modelDimensions(Integer dimensions) {
      this.dimensions = dimensions;
      return this;
    }
    @Override
    public EmbeddingService build() {
      return new AzureOpenAIService(this.azureOpenAIModelConnection, this.embeddingModelParameters, this.dimensions);
    }

}
