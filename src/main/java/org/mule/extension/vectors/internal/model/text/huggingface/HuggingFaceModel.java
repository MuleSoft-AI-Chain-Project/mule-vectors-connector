package org.mule.extension.vectors.internal.model.text.huggingface;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class HuggingFaceModel  extends BaseModel {

  private final String apiKey;

  public HuggingFaceModel(EmbeddingConfiguration embeddingConfiguration, HuggingFaceModelConnection huggingFaceModelConnection, EmbeddingModelParameters embeddingModelParameters) {

    super(embeddingConfiguration, huggingFaceModelConnection, embeddingModelParameters);

    this.apiKey = huggingFaceModelConnection.getApiKey();
  }

  public EmbeddingModel buildEmbeddingModel() {

    return HuggingFaceEmbeddingModel.builder()
        .accessToken(apiKey)
        .modelId(embeddingModelParameters.getEmbeddingModelName())
        .build();
  }
}
