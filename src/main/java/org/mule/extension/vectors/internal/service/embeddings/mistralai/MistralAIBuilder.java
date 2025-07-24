package org.mule.extension.vectors.internal.service.embeddings.mistralai;

import org.mule.extension.vectors.internal.connection.provider.embeddings.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceBuilder;

/**
 * MistralAIBuilder constructs MistralAIService for embedding operations.
 *
 * <p>Preferred usage: Use via EmbeddingServiceFactoryBuilder.getBuilder(...) as per MDC rule
 * 'Direct Construction of EmbeddingServiceBuilder via EmbeddingServiceFactoryBuilder'.
 * This bypasses the provider indirection for clarity and maintainability.</p>
 */
public class MistralAIBuilder implements EmbeddingServiceBuilder {

  private MistralAIModelConnection mistralAIModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;


  public MistralAIBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
    this.embeddingModelParameters = embeddingModelParameters;
    return this;
  }

  public MistralAIBuilder modelConnections(MistralAIModelConnection mistralAIModelConnection) {
    this.mistralAIModelConnection = mistralAIModelConnection;
    return this;
  }

  @Override
  public EmbeddingService build() {
    return new MistralAIService(this.mistralAIModelConnection, this.embeddingModelParameters);
  }

}

