package org.mule.extension.vectors.internal.service.embedding;

import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.azureopenai.AzureopenAIServiceProvider;
import org.mule.extension.vectors.internal.model.einstein.EinsteinServiceProvider;
import org.mule.extension.vectors.internal.model.huggingface.HuggingFaceServiceProvider;
import org.mule.extension.vectors.internal.model.mistralai.MistralAIServiceProvider;
import org.mule.extension.vectors.internal.model.nomic.NomicServiceProvider;
import org.mule.extension.vectors.internal.model.ollama.OllamaServiceProvider;
import org.mule.extension.vectors.internal.model.openai.OpenAIServiceProvider;
import org.mule.extension.vectors.internal.model.vertexai.VertexAIServiceProvider;

public class EmbeddingServiceFactoryBuilder {

  protected EmbeddingConfiguration embeddingConfiguration;
  protected BaseModelConnection modelConnection;
  protected EmbeddingModelParameters embeddingModelParameters;
  EmbeddingServiceProvider serviceProvider;

  public EmbeddingServiceFactoryBuilder(BaseModelConnection modelConnection) {
    this.modelConnection = modelConnection;
  }

  public EmbeddingServiceProvider getServiceProvider() {
    switch (modelConnection.getEmbeddingModelService()) {

      case Constants.EMBEDDING_MODEL_SERVICE_AZURE_OPENAI:
        serviceProvider = new AzureopenAIServiceProvider();
      
      case Constants.EMBEDDING_MODEL_SERVICE_EINSTEIN:
        serviceProvider = new EinsteinServiceProvider();

      case Constants.EMBEDDING_MODEL_SERVICE_HUGGING_FACE:
        serviceProvider = new HuggingFaceServiceProvider();
      
      case Constants.EMBEDDING_MODEL_SERVICE_MISTRAL_AI:
        serviceProvider = new MistralAIServiceProvider();

      case Constants.EMBEDDING_MODEL_SERVICE_NOMIC:
        serviceProvider = new NomicServiceProvider();

      case Constants.EMBEDDING_MODEL_SERVICE_OLLAMA:
        serviceProvider = new OllamaServiceProvider();

      case Constants.EMBEDDING_MODEL_SERVICE_OPENAI:
        serviceProvider = new OpenAIServiceProvider();

      case Constants.EMBEDDING_MODEL_SERVICE_VERTEX_AI:
        serviceProvider = new VertexAIServiceProvider();
    }
    return serviceProvider;
  }
}
