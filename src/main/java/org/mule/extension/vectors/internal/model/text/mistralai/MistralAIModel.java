package org.mule.extension.vectors.internal.model.text.mistralai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class MistralAIModel  extends BaseModel {

  private final String apiKey;

  public MistralAIModel(EmbeddingConfiguration embeddingConfiguration, MistralAIModelConnection mistralAIModelConnection, EmbeddingModelParameters embeddingModelParameters) {

    super(embeddingConfiguration, mistralAIModelConnection, embeddingModelParameters);

    this.apiKey = mistralAIModelConnection.getApiKey();
  }

  public EmbeddingModel buildEmbeddingModel() {

    return MistralAiEmbeddingModel.builder()
        .apiKey(apiKey)
        .modelName(embeddingModelParameters.getEmbeddingModelName())
        .build();
  }
}
