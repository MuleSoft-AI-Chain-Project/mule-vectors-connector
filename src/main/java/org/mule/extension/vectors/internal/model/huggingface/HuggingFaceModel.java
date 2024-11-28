package org.mule.extension.vectors.internal.model.huggingface;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import org.mule.extension.vectors.internal.config.Configuration;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class HuggingFaceModel  extends BaseModel {

  private final String apiKey;

  public HuggingFaceModel(HuggingFaceModelConnection huggingFaceModelConnection, EmbeddingModelParameters embeddingModelParameters) {

    super(huggingFaceModelConnection,embeddingModelParameters);

    this.apiKey = huggingFaceModelConnection.getApiKey();
  }

  public EmbeddingModel buildEmbeddingModel() {

    return HuggingFaceEmbeddingModel.builder()
        .accessToken(apiKey)
        .modelId(embeddingModelParameters.getEmbeddingModelName())
        .build();
  }
}
