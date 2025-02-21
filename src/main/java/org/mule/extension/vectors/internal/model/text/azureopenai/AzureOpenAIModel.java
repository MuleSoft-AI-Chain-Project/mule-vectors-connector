package org.mule.extension.vectors.internal.model.text.azureopenai;

import com.azure.ai.openai.OpenAIClient;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.azureopenai.AzureOpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureOpenAIModel extends BaseModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureOpenAIModel.class);

  private final String endpoint;
  private final String apiKey;
  private OpenAIClient openAIClient;

  public OpenAIClient getOpenAIClient() {

    if(this.openAIClient == null) {

      try {

        AzureOpenAIModelConnection azureOpenAIModelConnection = new AzureOpenAIModelConnection(apiKey,endpoint);
        azureOpenAIModelConnection.connect();
        this.openAIClient = azureOpenAIModelConnection.getOpenAIClient();

      } catch(Exception e ){

        LOGGER.error("Impossible to initiate OPENAI Client", e);
      }
    }
    return this.openAIClient;
  }

  public AzureOpenAIModel(EmbeddingConfiguration embeddingConfiguration,
                          AzureOpenAIModelConnection azureOpenAIModelConnection,
                          EmbeddingModelParameters embeddingModelParameters) {

    super(embeddingConfiguration, azureOpenAIModelConnection, embeddingModelParameters);

    this.endpoint = azureOpenAIModelConnection.getEndpoint();
    this.apiKey = azureOpenAIModelConnection.getApiKey();
    this.openAIClient = azureOpenAIModelConnection.getOpenAIClient();
  }

  public EmbeddingModel buildEmbeddingModel() {

    return AzureOpenAiEmbeddingModel.builder()
        .openAIClient(getOpenAIClient())
        .deploymentName(embeddingModelParameters.getEmbeddingModelName())
        .build();
  }
}
