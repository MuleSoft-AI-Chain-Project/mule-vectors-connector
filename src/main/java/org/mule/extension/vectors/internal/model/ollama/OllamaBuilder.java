package org.mule.extension.vectors.internal.model.ollama;

import org.mule.extension.vectors.internal.connection.model.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

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
      return new OllamaService(this.ollamaModelConnection, this.embeddingModelParameters, this.dimensions);
    }

}


