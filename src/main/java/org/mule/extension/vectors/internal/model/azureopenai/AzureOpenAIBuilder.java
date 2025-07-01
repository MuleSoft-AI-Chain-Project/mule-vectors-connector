package org.mule.extension.vectors.internal.model.azureopenai;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

public class AzureOpenAIBuilder implements EmbeddingServiceBuilder {
    private AzureOpenAIModelConnection azureOpenAIModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;


    public AzureOpenAIBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public AzureOpenAIBuilder modelConnections(AzureOpenAIModelConnection azureOpenAIModelConnection) {
      this.azureOpenAIModelConnection = azureOpenAIModelConnection;
      return this;
    }
    public AzureOpenAIBuilder modelDimensions(Integer dimensions) {
      this.dimensions = dimensions;
      return this;
    }
    @Override
    public EmbeddingService build() {
      return new AzureOpenAIService(this.azureOpenAIModelConnection, this.embeddingModelParameters, this.dimensions);
    }

}
