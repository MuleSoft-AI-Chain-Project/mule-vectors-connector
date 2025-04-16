package org.mule.extension.vectors.internal.model.text.azureopenai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class AzureOpenAIModel extends BaseModel {

  private final String modelName;
  private final AzureOpenAIModelConnection azureOpenAIModelConnection;

  public AzureOpenAIModel(EmbeddingConfiguration embeddingConfiguration,
                          AzureOpenAIModelConnection azureOpenAIModelConnection,
                          EmbeddingModelParameters embeddingModelParameters) {

    super(embeddingConfiguration, azureOpenAIModelConnection, embeddingModelParameters);

    this.modelName = embeddingModelParameters.getEmbeddingModelName();
    this.azureOpenAIModelConnection = azureOpenAIModelConnection;
  }

  public EmbeddingModel buildEmbeddingModel() {

    return AzureOpenAIEmbeddingModel.builder()
        .connection(azureOpenAIModelConnection)
        .modelName(modelName)
        .build();
  }
}
