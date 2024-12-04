package org.mule.extension.vectors.internal.connection.model.mistralai;

import org.mule.extension.vectors.internal.connection.model.BaseModelConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

@Alias("mistralAI")
@DisplayName("Mistral AI")
public class MistralAIModelConnection implements BaseModelConnection {

  private String apiKey;

  public MistralAIModelConnection(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiKey() {
    return apiKey;
  }

  @Override
  public String getEmbeddingModelService() {
    return Constants.EMBEDDING_MODEL_SERVICE_MISTRAL_AI;
  }
}
