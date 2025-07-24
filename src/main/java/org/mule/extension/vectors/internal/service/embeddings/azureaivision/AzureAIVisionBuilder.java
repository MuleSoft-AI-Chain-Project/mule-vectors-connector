package org.mule.extension.vectors.internal.service.embeddings.azureaivision;

import org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceBuilder;

/**
 * AzureAIVisionBuilder constructs AzureAIVisionService for embedding operations.
 *
 * <p>Preferred usage: Use via EmbeddingServiceFactoryBuilder.getBuilder(...) as per MDC rule
 * 'Direct Construction of EmbeddingServiceBuilder via EmbeddingServiceFactoryBuilder'.
 * This bypasses the provider indirection for clarity and maintainability.</p>
 */
public class AzureAIVisionBuilder implements EmbeddingServiceBuilder {

  private AzureAIVisionModelConnection azureAIVisionModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;


  public AzureAIVisionBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
    this.embeddingModelParameters = embeddingModelParameters;
    return this;
  }

  public AzureAIVisionBuilder modelConnections(AzureAIVisionModelConnection azureAIVisionModelConnection) {
    this.azureAIVisionModelConnection = azureAIVisionModelConnection;
    return this;
  }

  @Override
  public EmbeddingService build() {
    return new AzureAIVisionService(this.azureAIVisionModelConnection, this.embeddingModelParameters);
  }

}
