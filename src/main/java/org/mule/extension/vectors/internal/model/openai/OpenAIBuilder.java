package org.mule.extension.vectors.internal.model.openai;

import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

public class OpenAIBuilder implements EmbeddingServiceBuilder {
    private OpenAIModelConnection openAIModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;


    public OpenAIBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public OpenAIBuilder modelConnections(OpenAIModelConnection openAIModelConnection) {
      this.openAIModelConnection = openAIModelConnection;
      return this;
    }
    public OpenAIBuilder modelDimensions(Integer dimensions) {
      this.dimensions = dimensions;
      return this;
    }
    @Override
    public EmbeddingService build() {
      return new OpenAIService(this.openAIModelConnection, this.embeddingModelParameters, this.dimensions);
    }

}



