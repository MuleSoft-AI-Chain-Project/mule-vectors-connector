package org.mule.extension.vectors.internal.service.embeddings;

import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.azureaivision.AzureAIVisionBuilder;

import org.mule.extension.vectors.internal.service.embeddings.azureopenai.AzureOpenAIBuilder;

import org.mule.extension.vectors.internal.service.embeddings.einstein.EinsteinBuilder;

import org.mule.extension.vectors.internal.service.embeddings.huggingface.HuggingFaceBuilder;

import org.mule.extension.vectors.internal.service.embeddings.mistralai.MistralAIBuilder;

import org.mule.extension.vectors.internal.service.embeddings.nomic.NomicBuilder;

import org.mule.extension.vectors.internal.service.embeddings.ollama.OllamaBuilder;

import org.mule.extension.vectors.internal.service.embeddings.openai.OpenAIBuilder;

import org.mule.extension.vectors.internal.service.embeddings.vertexai.VertexAIBuilder;


public class EmbeddingServiceFactoryBuilder {

  protected EmbeddingConfiguration embeddingConfiguration;
  protected BaseModelConnection modelConnection;
  protected EmbeddingModelParameters embeddingModelParameters;
  EmbeddingServiceProvider serviceProvider;

  public EmbeddingServiceFactoryBuilder(BaseModelConnection modelConnection) {
    this.modelConnection = modelConnection;
  }

  /**
   * Returns the appropriate EmbeddingServiceBuilder for the given model connection and parameters.
   * This bypasses the EmbeddingServiceProvider indirection.
   */
  public EmbeddingServiceBuilder getBuilder(BaseModelConnection modelConnection, EmbeddingModelParameters embeddingModelParameters) {
    switch (modelConnection.getEmbeddingModelService()) {
      case Constants.EMBEDDING_MODEL_SERVICE_AZURE_AI_VISION:
        return new AzureAIVisionBuilder()
            .modelParameters(embeddingModelParameters)
            .modelConnections((org.mule.extension.vectors.internal.connection.embeddings.azureaivision.AzureAIVisionModelConnection) modelConnection);
      case Constants.EMBEDDING_MODEL_SERVICE_AZURE_OPENAI:
        return new AzureOpenAIBuilder()
            .modelParameters(embeddingModelParameters)
            .modelConnections((org.mule.extension.vectors.internal.connection.embeddings.azureopenai.AzureOpenAIModelConnection) modelConnection);
      case Constants.EMBEDDING_MODEL_SERVICE_EINSTEIN:
        return new EinsteinBuilder()
            .modelParameters(embeddingModelParameters)
            .modelConnections((org.mule.extension.vectors.internal.connection.embeddings.einstein.EinsteinModelConnection) modelConnection);
      case Constants.EMBEDDING_MODEL_SERVICE_HUGGING_FACE:
        return new HuggingFaceBuilder()
            .modelParameters(embeddingModelParameters)
            .modelConnections((org.mule.extension.vectors.internal.connection.embeddings.huggingface.HuggingFaceModelConnection) modelConnection);
      case Constants.EMBEDDING_MODEL_SERVICE_MISTRAL_AI:
        return new MistralAIBuilder()
            .modelParameters(embeddingModelParameters)
            .modelConnections((org.mule.extension.vectors.internal.connection.embeddings.mistralai.MistralAIModelConnection) modelConnection);
      case Constants.EMBEDDING_MODEL_SERVICE_NOMIC:
        return new NomicBuilder()
            .modelParameters(embeddingModelParameters)
            .modelConnections((org.mule.extension.vectors.internal.connection.embeddings.nomic.NomicModelConnection) modelConnection);
      case Constants.EMBEDDING_MODEL_SERVICE_OLLAMA:
        return new OllamaBuilder()
            .modelParameters(embeddingModelParameters)
            .modelConnections((org.mule.extension.vectors.internal.connection.embeddings.ollama.OllamaModelConnection) modelConnection);
      case Constants.EMBEDDING_MODEL_SERVICE_OPENAI:
        return new OpenAIBuilder()
            .modelParameters(embeddingModelParameters)
            .modelConnections((org.mule.extension.vectors.internal.connection.embeddings.openai.OpenAIModelConnection) modelConnection);
      case Constants.EMBEDDING_MODEL_SERVICE_VERTEX_AI:
        return new VertexAIBuilder()
            .modelParameters(embeddingModelParameters)
            .modelConnections((org.mule.extension.vectors.internal.connection.embeddings.vertexai.VertexAIModelConnection) modelConnection);
      default:
        throw new IllegalArgumentException("Unsupported embedding model service: " + modelConnection.getEmbeddingModelService());
    }
  }
}
