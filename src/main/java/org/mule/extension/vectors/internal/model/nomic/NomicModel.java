package org.mule.extension.vectors.internal.model.nomic;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.nomic.NomicEmbeddingModel;
import org.mule.extension.vectors.internal.config.Configuration;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class NomicModel  extends BaseModel {

  private final String apiKey;

  public NomicModel(NomicModelConnection nomicModelConnection, EmbeddingModelParameters embeddingModelParameters) {

    super(nomicModelConnection,embeddingModelParameters);

    this.apiKey = nomicModelConnection.getApiKey();
  }

  public EmbeddingModel buildEmbeddingModel() {

    return NomicEmbeddingModel.builder()
        //.baseUrl("https://api-atlas.nomic.ai/v1/")
        .apiKey(apiKey)
        .modelName(embeddingModelParameters.getEmbeddingModelName())
        //.taskType("clustering")
        .maxRetries(2)
        .logRequests(true)
        .logResponses(true)
        .build();
  }
}
