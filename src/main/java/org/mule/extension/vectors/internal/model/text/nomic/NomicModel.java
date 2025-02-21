package org.mule.extension.vectors.internal.model.text.nomic;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.nomic.NomicEmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class NomicModel  extends BaseModel {

  private NomicModelConnection nomicModelConnection;

  public NomicModel(EmbeddingConfiguration embeddingConfiguration, NomicModelConnection nomicModelConnection, EmbeddingModelParameters embeddingModelParameters) {

    super(embeddingConfiguration, nomicModelConnection, embeddingModelParameters);

    this.nomicModelConnection = nomicModelConnection;
  }

  public EmbeddingModel buildEmbeddingModel() {

    return NomicEmbeddingModel.builder()
        //.baseUrl("https://api-atlas.nomic.ai/v1/")
        .apiKey(nomicModelConnection.getApiKey())
        .modelName(embeddingModelParameters.getEmbeddingModelName())
        .maxRetries(nomicModelConnection.getMaxAttempts())
        .build();
  }
}
