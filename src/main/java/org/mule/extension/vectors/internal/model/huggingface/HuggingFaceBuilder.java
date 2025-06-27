package org.mule.extension.vectors.internal.model.huggingface;

import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

public class HuggingFaceBuilder implements EmbeddingServiceBuilder {
    private HuggingFaceModelConnection huggingFaceModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;


    public HuggingFaceBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public HuggingFaceBuilder modelConnections(HuggingFaceModelConnection huggingFaceModelConnection) {
      this.huggingFaceModelConnection = huggingFaceModelConnection;
      return this;
    }
    public HuggingFaceBuilder modelDimensions(Integer dimensions) {
      this.dimensions = dimensions;
      return this;
    }
    @Override
    public EmbeddingService build() {
      return new HuggingFaceService(this.huggingFaceModelConnection, this.embeddingModelParameters, this.dimensions);
    }

}

