package org.mule.extension.vectors.internal.model.vertexai;

import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

/**
 * VertexAIBuilder constructs VertexAIService for embedding operations.
 *
 * <p>Preferred usage: Use via EmbeddingServiceFactoryBuilder.getBuilder(...) as per MDC rule
 * 'Direct Construction of EmbeddingServiceBuilder via EmbeddingServiceFactoryBuilder'.
 * This bypasses the provider indirection for clarity and maintainability.</p>
 */
public class VertexAIBuilder implements EmbeddingServiceBuilder {
    private VertexAIModelConnection vertexAIModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;


    public VertexAIBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public VertexAIBuilder modelConnections(VertexAIModelConnection vertexAIModelConnection) {
      this.vertexAIModelConnection = vertexAIModelConnection;
      return this;
    }
    public VertexAIBuilder modelDimensions(Integer dimensions) {
      this.dimensions = dimensions;
      return this;
    }
    @Override
    public EmbeddingService build() {
      return new VertexAIService(this.vertexAIModelConnection, this.embeddingModelParameters, this.dimensions);
    }

}




