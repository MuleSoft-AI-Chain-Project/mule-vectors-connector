package org.mule.extension.vectors.internal.service.embedding;

import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.model.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.azureopenai.AzureOpenAIBuilder;
import org.mule.extension.vectors.internal.model.azureopenai.AzureopenAIServiceProvider;
import org.mule.extension.vectors.internal.model.text.azureopenai.AzureOpenAIModel;

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
    }
    return serviceProvider;
  }
}
