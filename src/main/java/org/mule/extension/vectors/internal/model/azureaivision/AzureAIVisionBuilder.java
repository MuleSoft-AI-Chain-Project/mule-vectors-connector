package org.mule.extension.vectors.internal.model.azureaivision;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;

public class AzureAIVisionBuilder implements EmbeddingServiceBuilder {
    private AzureAIVisionModelConnection azureAIVisionModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;


    public AzureAIVisionBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public AzureAIVisionBuilder modelConnections(AzureAIVisionModelConnection azureAIVisionModelConnection) {
      this.azureAIVisionModelConnection = azureAIVisionModelConnection;
      return this;
    }
    public AzureAIVisionBuilder modelDimensions(Integer dimensions) {
      this.dimensions =dimensions;
      return this;
    }
    @Override
    public EmbeddingService build() {
      return new AzureAIVisionService(this.azureAIVisionModelConnection, this.embeddingModelParameters, this.dimensions);
    }

}
