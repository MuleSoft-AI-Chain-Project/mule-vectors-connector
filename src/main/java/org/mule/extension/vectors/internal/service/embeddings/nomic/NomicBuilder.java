package org.mule.extension.vectors.internal.service.embeddings.nomic;

import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceBuilder;

/**
 * NomicBuilder constructs NomicService for embedding operations.
 *
 * <p>Preferred usage: Use via EmbeddingServiceFactoryBuilder.getBuilder(...) as per MDC rule
 * 'Direct Construction of EmbeddingServiceBuilder via EmbeddingServiceFactoryBuilder'.
 * This bypasses the provider indirection for clarity and maintainability.</p>
 */
public class NomicBuilder implements EmbeddingServiceBuilder {
    private NomicModelConnection nomicModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;


    public NomicBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public NomicBuilder modelConnections(NomicModelConnection nomicModelConnection) {
      this.nomicModelConnection = nomicModelConnection;
      return this;
    }
    public NomicBuilder modelDimensions(Integer dimensions) {
      this.dimensions = dimensions;
      return this;
    }
    @Override
    public EmbeddingService build() {
      return new NomicService(this.nomicModelConnection, this.embeddingModelParameters, this.dimensions);
    }

}

