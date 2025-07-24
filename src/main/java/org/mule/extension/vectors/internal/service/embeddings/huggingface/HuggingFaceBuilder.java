package org.mule.extension.vectors.internal.service.embeddings.huggingface;

import org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceBuilder;

/**
 * HuggingFaceBuilder constructs HuggingFaceService for embedding operations.
 *
 * <p>Preferred usage: Use via EmbeddingServiceFactoryBuilder.getBuilder(...) as per MDC rule
 * 'Direct Construction of EmbeddingServiceBuilder via EmbeddingServiceFactoryBuilder'.
 * This bypasses the provider indirection for clarity and maintainability.</p>
 */
public class HuggingFaceBuilder implements EmbeddingServiceBuilder {

  private HuggingFaceModelConnection huggingFaceModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;


  public HuggingFaceBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
    this.embeddingModelParameters = embeddingModelParameters;
    return this;
  }

  public HuggingFaceBuilder modelConnections(HuggingFaceModelConnection huggingFaceModelConnection) {
    this.huggingFaceModelConnection = huggingFaceModelConnection;
    return this;
  }

  @Override
  public EmbeddingService build() {
    return new HuggingFaceService(this.huggingFaceModelConnection, this.embeddingModelParameters);
  }

}

