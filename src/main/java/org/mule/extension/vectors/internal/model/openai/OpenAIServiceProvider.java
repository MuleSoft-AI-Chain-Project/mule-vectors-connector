package org.mule.extension.vectors.internal.model.openai;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceProvider;

public class OpenAIServiceProvider implements EmbeddingServiceProvider {

  @Override
  public EmbeddingServiceBuilder getBuilder(BaseModelConnection baseModelConnection,
                                            EmbeddingModelParameters embeddingModelParameters) {
    OpenAIBuilder builder =  new OpenAIBuilder().modelParameters(embeddingModelParameters).modelConnections(
        (OpenAIModelConnection) baseModelConnection);
    return builder;
  }
}


