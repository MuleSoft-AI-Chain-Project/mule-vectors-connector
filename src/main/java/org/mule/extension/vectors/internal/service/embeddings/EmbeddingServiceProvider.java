package org.mule.extension.vectors.internal.service.embeddings;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;

public interface EmbeddingServiceProvider {

  public EmbeddingServiceBuilder getBuilder(BaseModelConnection baseModelConnection,
                                            EmbeddingModelParameters embeddingModelParameters);
}
