package org.mule.extension.vectors.internal.model.vertexai;


import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceProvider;

public class VertexAIServiceProvider implements EmbeddingServiceProvider {

  @Override
  public EmbeddingServiceBuilder getBuilder(BaseModelConnection baseModelConnection,
                                            EmbeddingModelParameters embeddingModelParameters) {
    VertexAIBuilder builder =  new VertexAIBuilder().modelParameters(embeddingModelParameters).modelConnections(
        (VertexAIModelConnection) baseModelConnection);
    return builder;
  }
}


