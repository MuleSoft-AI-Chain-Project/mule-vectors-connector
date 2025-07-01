package org.mule.extension.vectors.internal.model.nomic;

import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

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

