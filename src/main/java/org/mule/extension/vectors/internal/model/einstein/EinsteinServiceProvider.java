package org.mule.extension.vectors.internal.model.einstein;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceProvider;

public class EinsteinServiceProvider implements EmbeddingServiceProvider {

  @Override
  public EmbeddingServiceBuilder getBuilder(BaseModelConnection baseModelConnection,
                                            EmbeddingModelParameters embeddingModelParameters) {
    EinsteinBuilder builder =  new EinsteinBuilder().modelParameters(embeddingModelParameters).modelConnections(
        (EinsteinModelConnection) baseModelConnection);
    return builder;
  }
}
