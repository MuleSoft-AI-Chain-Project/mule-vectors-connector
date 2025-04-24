package org.mule.extension.vectors.internal.model.multimodal.nomic;

import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;
import org.mule.extension.vectors.internal.model.multimodal.EmbeddingMultimodalModel;
import org.mule.runtime.extension.api.exception.ModuleException;

public class NomicMultimodalModel extends BaseModel {

  private final NomicModelConnection nomicModelConnection;

  public NomicMultimodalModel(EmbeddingConfiguration embeddingConfiguration,
                             NomicModelConnection nomicModelConnection,
                             EmbeddingModelParameters embeddingModelParameters) {
    super(embeddingConfiguration, nomicModelConnection, embeddingModelParameters);
    try {
      this.nomicModelConnection = nomicModelConnection;
    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error initializing Nomic Model."),
          MuleVectorsErrorType.AI_SERVICES_FAILURE,
          e);
    }
  }

  public EmbeddingMultimodalModel buildEmbeddingMultimodalModel() {
    try {
      return NomicEmbeddingMultimodalModel.builder()
          .connection(nomicModelConnection)
          .modelName(embeddingModelParameters.getEmbeddingModelName())
          .build();
    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error initializing Nomic Embedding Model."),
          MuleVectorsErrorType.AI_SERVICES_FAILURE,
          e);
    }
  }
}
