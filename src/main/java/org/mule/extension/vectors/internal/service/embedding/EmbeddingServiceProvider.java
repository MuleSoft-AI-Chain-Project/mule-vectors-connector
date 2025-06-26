package org.mule.extension.vectors.internal.service.embedding;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;

public interface EmbeddingServiceProvider {

  public EmbeddingServiceBuilder getBuilder(BaseModelConnection baseModelConnection, EmbeddingModelParameters embeddingModelParameters);
}
