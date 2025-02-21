package org.mule.extension.vectors.internal.model;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.connection.model.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.connection.model.einstein.EinsteinModelConnection;
import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.connection.model.mistralai.MistralAIModelConnection;
import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.connection.model.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.multimodal.EmbeddingMultimodalModel;
import org.mule.extension.vectors.internal.model.multimodal.azureaivision.AzureAIVisionEmbeddingMultimodalModel;
import org.mule.extension.vectors.internal.model.multimodal.azureaivision.AzureAIVisionMultimodalModel;
import org.mule.extension.vectors.internal.model.multimodal.nomic.NomicMultimodalModel;
import org.mule.extension.vectors.internal.model.multimodal.vertexai.VertexAIMultimodalModel;
import org.mule.extension.vectors.internal.model.text.azureopenai.AzureOpenAIModel;
import org.mule.extension.vectors.internal.model.text.einstein.EinsteinModel;
import org.mule.extension.vectors.internal.model.text.huggingface.HuggingFaceModel;
import org.mule.extension.vectors.internal.model.text.mistralai.MistralAIModel;
import org.mule.extension.vectors.internal.model.text.nomic.NomicModel;
import org.mule.extension.vectors.internal.model.text.ollama.OllamaModel;
import org.mule.extension.vectors.internal.model.text.openai.OpenAIModel;
import org.mule.extension.vectors.internal.model.text.vertexai.VertexAIModel;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseModel.class);

  protected EmbeddingConfiguration embeddingConfiguration;
  protected BaseModelConnection modelConnection;
  protected EmbeddingModelParameters embeddingModelParameters;

  public BaseModel(EmbeddingConfiguration embeddingConfiguration, BaseModelConnection modelConnection, EmbeddingModelParameters embeddingModelParameters) {

    this.embeddingConfiguration = embeddingConfiguration;
    this.modelConnection = modelConnection;
    this.embeddingModelParameters = embeddingModelParameters;
  }

  public EmbeddingModel buildEmbeddingModel() {

    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  public EmbeddingMultimodalModel buildEmbeddingMultimodalModel() {

    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  public static BaseModel.Builder builder() {

    return new BaseModel.Builder();
  }

  public static class Builder {

    private EmbeddingConfiguration embeddingConfiguration;
    protected BaseModelConnection modelConnection;
    private EmbeddingModelParameters embeddingModelParameters;

    public Builder() {

    }

    public BaseModel.Builder configuration(EmbeddingConfiguration embeddingConfiguration) {
      this.embeddingConfiguration = embeddingConfiguration;
      return this;
    }

    public BaseModel.Builder connection(BaseModelConnection modelConnection) {
      this.modelConnection = modelConnection;
      return this;
    }

    public BaseModel.Builder embeddingModelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }

    public BaseModel build() {

      BaseModel baseModel;

      LOGGER.debug("Embedding Model Service: " + modelConnection.getEmbeddingModelService());
      LOGGER.debug("Embedding Model type: " + embeddingModelParameters.getEmbeddingModelType().getDescription());

      switch (modelConnection.getEmbeddingModelService()) {

        case Constants.EMBEDDING_MODEL_SERVICE_AZURE_OPENAI:
          baseModel = new AzureOpenAIModel(embeddingConfiguration, (AzureOpenAIModelConnection) modelConnection, embeddingModelParameters);
          break;

        case Constants.EMBEDDING_MODEL_SERVICE_AZURE_AI_VISION:
          baseModel = new AzureAIVisionMultimodalModel(embeddingConfiguration, (AzureAIVisionModelConnection) modelConnection, embeddingModelParameters);
          break;

        case Constants.EMBEDDING_MODEL_SERVICE_OPENAI:
          baseModel = new OpenAIModel(embeddingConfiguration, (OpenAIModelConnection) modelConnection, embeddingModelParameters);
          break;

        case Constants.EMBEDDING_MODEL_SERVICE_MISTRAL_AI:
          baseModel = new MistralAIModel(embeddingConfiguration, (MistralAIModelConnection) modelConnection, embeddingModelParameters);
          break;

        case Constants.EMBEDDING_MODEL_SERVICE_NOMIC:

          if(embeddingModelParameters.getEmbeddingModelType().equals(EmbeddingModelHelper.EmbeddingModelType.MULTIMODAL)) {

            baseModel = new NomicMultimodalModel(embeddingConfiguration, (NomicModelConnection) modelConnection, embeddingModelParameters);
            break;
          }

          baseModel = new NomicModel(embeddingConfiguration, (NomicModelConnection) modelConnection, embeddingModelParameters);
          break;

        case Constants.EMBEDDING_MODEL_SERVICE_OLLAMA:
          baseModel = new OllamaModel(embeddingConfiguration, (OllamaModelConnection) modelConnection, embeddingModelParameters);
          break;

        case Constants.EMBEDDING_MODEL_SERVICE_HUGGING_FACE:
          baseModel = new HuggingFaceModel(embeddingConfiguration, (HuggingFaceModelConnection) modelConnection, embeddingModelParameters);
          break;

        case Constants.EMBEDDING_MODEL_SERVICE_EINSTEIN:
          baseModel = new EinsteinModel(embeddingConfiguration, (EinsteinModelConnection) modelConnection, embeddingModelParameters);
          break;

        case Constants.EMBEDDING_MODEL_SERVICE_VERTEX_AI:

          if(embeddingModelParameters.getEmbeddingModelType().equals(EmbeddingModelHelper.EmbeddingModelType.MULTIMODAL)) {

            baseModel = new VertexAIMultimodalModel(embeddingConfiguration, (VertexAIModelConnection) modelConnection, embeddingModelParameters);
            break;
          }

          baseModel = new VertexAIModel(embeddingConfiguration, (VertexAIModelConnection) modelConnection, embeddingModelParameters);
          break;

        default:
          throw new ModuleException(
              String.format("Error while initializing embedding model service. \"%s\" is not supported.", modelConnection.getEmbeddingModelService()),
              MuleVectorsErrorType.AI_SERVICES_FAILURE);
      }
      return baseModel;
    }
  }
}
