package org.mule.extension.vectors.internal.model.mistralai;

import org.mule.extension.vectors.internal.connection.model.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

public class MistralAIBuilder implements EmbeddingServiceBuilder {
    private MistralAIModelConnection mistralAIModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;


    public MistralAIBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public MistralAIBuilder modelConnections(MistralAIModelConnection mistralAIModelConnection) {
      this.mistralAIModelConnection = mistralAIModelConnection;
      return this;
    }
    public MistralAIBuilder modelDimensions(Integer dimensions) {
      this.dimensions = dimensions;
      return this;
    }
    @Override
    public EmbeddingService build() {
      return new MistralAIService(this.mistralAIModelConnection, this.embeddingModelParameters, this.dimensions);
    }

}

