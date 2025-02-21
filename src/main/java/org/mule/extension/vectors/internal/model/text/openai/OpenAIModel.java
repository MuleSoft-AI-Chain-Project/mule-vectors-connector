package org.mule.extension.vectors.internal.model.text.openai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class OpenAIModel  extends BaseModel {

  private final String apiKey;

  public OpenAIModel(EmbeddingConfiguration embeddingConfiguration, OpenAIModelConnection openAIModelConnection, EmbeddingModelParameters embeddingModelParameters) {

    super(embeddingConfiguration, openAIModelConnection, embeddingModelParameters);

    this.apiKey = openAIModelConnection.getApiKey();
  }

  public EmbeddingModel buildEmbeddingModel() {

    return OpenAiEmbeddingModel.builder()
        .apiKey(apiKey)
        .modelName(embeddingModelParameters.getEmbeddingModelName())
        .build();
  }
}
