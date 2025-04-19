package org.mule.extension.vectors.internal.model.text.ollama;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;

public class OllamaModel extends BaseModel {

  private OllamaModelConnection ollamaModelConnection;

  public OllamaModel(EmbeddingConfiguration embeddingConfiguration, OllamaModelConnection ollamaModelConnection, EmbeddingModelParameters embeddingModelParameters) {
    super(embeddingConfiguration, ollamaModelConnection, embeddingModelParameters);
    this.ollamaModelConnection = ollamaModelConnection;
  }

  public EmbeddingModel buildEmbeddingModel() {
    return OllamaEmbeddingModel.builder()
        .connection(ollamaModelConnection)
        .modelName(embeddingModelParameters.getEmbeddingModelName())
        .build();
  }
}
