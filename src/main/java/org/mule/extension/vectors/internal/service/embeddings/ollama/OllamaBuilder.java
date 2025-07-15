package org.mule.extension.vectors.internal.service.embeddings.ollama;

import org.mule.extension.vectors.internal.connection.embeddings.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceBuilder;

/**
 * OllamaBuilder constructs OllamaService for embedding operations.
 *
 * <p>Preferred usage: Use via EmbeddingServiceFactoryBuilder.getBuilder(...) as per MDC rule
 * 'Direct Construction of EmbeddingServiceBuilder via EmbeddingServiceFactoryBuilder'.
 * This bypasses the provider indirection for clarity and maintainability.</p>
 */
public class OllamaBuilder implements EmbeddingServiceBuilder {
    private OllamaModelConnection ollamaModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;


    public OllamaBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public OllamaBuilder modelConnections(OllamaModelConnection ollamaModelConnection) {
      this.ollamaModelConnection = ollamaModelConnection;
      return this;
    }
    public OllamaBuilder modelDimensions(Integer dimensions) {
      this.dimensions = dimensions;
      return this;
    }
    @Override
    public EmbeddingService build() {
      return new OllamaService(this.ollamaModelConnection, this.embeddingModelParameters);
    }

}


