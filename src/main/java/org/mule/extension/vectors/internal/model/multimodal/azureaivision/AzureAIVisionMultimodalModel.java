package org.mule.extension.vectors.internal.model.multimodal.azureaivision;

import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;
import org.mule.extension.vectors.internal.model.multimodal.EmbeddingMultimodalModel;
import org.mule.extension.vectors.internal.model.multimodal.nomic.NomicEmbeddingMultimodalModel;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.time.Duration;

public class AzureAIVisionMultimodalModel  extends BaseModel {

  private final AzureAIVisionModelConnection azureAIVisionModelConnection;

  public AzureAIVisionMultimodalModel(EmbeddingConfiguration embeddingConfiguration,
                                      AzureAIVisionModelConnection azureAIVisionModelConnection,
                                      EmbeddingModelParameters embeddingModelParameters) {

    super(embeddingConfiguration, azureAIVisionModelConnection, embeddingModelParameters);
    try{

      this.azureAIVisionModelConnection = azureAIVisionModelConnection;

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error initializing Azure AI Vision Model."),
          MuleVectorsErrorType.AI_SERVICES_FAILURE,
          e);
    }
  }

  public EmbeddingMultimodalModel buildEmbeddingMultimodalModel() {

    try {

      return AzureAIVisionEmbeddingMultimodalModel.builder()
          .connection(azureAIVisionModelConnection)
          .modelName(embeddingModelParameters.getEmbeddingModelName())
          .build();

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error initializing Azure AI Vision Embedding Model."),
          MuleVectorsErrorType.AI_SERVICES_FAILURE,
          e);
    }
  }
}
