package org.mule.extension.vectors.internal.model.multimodal.vertexai;

import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;
import org.mule.extension.vectors.internal.model.multimodal.EmbeddingMultimodalModel;
import org.mule.runtime.extension.api.exception.ModuleException;

public class VertexAIMultimodalModel extends BaseModel {

  private final VertexAIModelConnection vertexAIModelConnection;

  public VertexAIMultimodalModel(EmbeddingConfiguration embeddingConfiguration,
                                 VertexAIModelConnection vertexAIModelConnection,
                                 EmbeddingModelParameters embeddingModelParameters) {

    super(embeddingConfiguration, vertexAIModelConnection, embeddingModelParameters);
    try{

      this.vertexAIModelConnection = vertexAIModelConnection;
    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error initializing Vertex AI Model."),
          MuleVectorsErrorType.AI_SERVICES_FAILURE,
          e);
    }
  }

  public EmbeddingMultimodalModel buildEmbeddingMultimodalModel() {

    try {

      return VertexAiEmbeddingMultimodalModel.builder()
          .connection(vertexAIModelConnection)
          .modelName(embeddingModelParameters.getEmbeddingModelName())
          .build();

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error initializing Vertex AI Embedding Model."),
          MuleVectorsErrorType.AI_SERVICES_FAILURE,
          e);
    }
  }
}
