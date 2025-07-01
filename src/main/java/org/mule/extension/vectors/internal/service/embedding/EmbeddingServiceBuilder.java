package org.mule.extension.vectors.internal.service.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;

public interface EmbeddingServiceBuilder {
  public EmbeddingService build();
}
