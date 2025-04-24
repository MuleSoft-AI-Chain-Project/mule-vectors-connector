package org.mule.extension.vectors.internal.model.text.openai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class OpenAIModel extends BaseModel {

  private final OpenAIModelConnection openAIModelConnection;

  public OpenAIModel(EmbeddingConfiguration embeddingConfiguration, OpenAIModelConnection openAIModelConnection, EmbeddingModelParameters embeddingModelParameters) {
    super(embeddingConfiguration, openAIModelConnection, embeddingModelParameters);
    this.openAIModelConnection = openAIModelConnection;
  }

  public EmbeddingModel buildEmbeddingModel() {
    return OpenAIEmbeddingModel.builder()
        .connection(openAIModelConnection)
        .modelName(embeddingModelParameters.getEmbeddingModelName())
        .build();
  }
}
