package org.mule.extension.vectors.internal.connection.provider.embeddings.mistralai;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;
@Alias("mistralAI")
@DisplayName("Mistral AI")
public class MistralAIModelConnectionProvider extends BaseModelConnectionProvider {


  @ParameterGroup(name = CONNECTION)
  private MistralAIModelConnectionParameters mistralAIModelConnectionParameters;

  @Override
  public BaseModelConnection connect() throws ConnectionException {
      MistralAIModelConnection mistralAIModelConnection = new MistralAIModelConnection(
          mistralAIModelConnectionParameters.getApiKey(),
          mistralAIModelConnectionParameters.getTotalTimeout(),
          getHttpClient());
      return mistralAIModelConnection;

  }
}
