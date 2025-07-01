package org.mule.extension.vectors.internal.model.huggingface;


import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceBuilder;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingServiceProvider;

public class HuggingFaceServiceProvider implements EmbeddingServiceProvider {

  @Override
  public EmbeddingServiceBuilder getBuilder(BaseModelConnection baseModelConnection,
                                            EmbeddingModelParameters embeddingModelParameters) {
    HuggingFaceBuilder builder =  new HuggingFaceBuilder().modelParameters(embeddingModelParameters).modelConnections(
        (HuggingFaceModelConnection) baseModelConnection);
    return builder;
  }
}

