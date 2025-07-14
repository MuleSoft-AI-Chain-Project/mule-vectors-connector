package org.mule.extension.vectors.internal.service.embeddings.einstein;

import org.mule.extension.vectors.internal.connection.model.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceBuilder;

/**
 * EinsteinBuilder constructs EinsteinService for embedding operations.
 *
 * <p>Preferred usage: Use via EmbeddingServiceFactoryBuilder.getBuilder(...) as per MDC rule
 * 'Direct Construction of EmbeddingServiceBuilder via EmbeddingServiceFactoryBuilder'.
 * This bypasses the provider indirection for clarity and maintainability.</p>
 */
public class EinsteinBuilder implements EmbeddingServiceBuilder {
    private EinsteinModelConnection einsteinModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;


    public EinsteinBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public EinsteinBuilder modelConnections(EinsteinModelConnection einsteinModelConnection) {
      this.einsteinModelConnection = einsteinModelConnection;
      return this;
    }
    public EinsteinBuilder modelDimensions(Integer dimensions) {
      this.dimensions =dimensions;
      return this;
    }
    @Override
    public EmbeddingService build() {
      return new EinsteinService(this.einsteinModelConnection, this.embeddingModelParameters, this.dimensions);
    }

}
