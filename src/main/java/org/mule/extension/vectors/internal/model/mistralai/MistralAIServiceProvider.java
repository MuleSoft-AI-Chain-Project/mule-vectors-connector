package org.mule.extension.vectors.internal.model.mistralai;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceProvider;

public class MistralAIServiceProvider implements EmbeddingServiceProvider {

  @Override
  public EmbeddingServiceBuilder getBuilder(BaseModelConnection baseModelConnection,
                                            EmbeddingModelParameters embeddingModelParameters) {
    MistralAIBuilder builder =  new MistralAIBuilder().modelParameters(embeddingModelParameters).modelConnections(
        (MistralAIModelConnection) baseModelConnection);
    return builder;
  }
}

