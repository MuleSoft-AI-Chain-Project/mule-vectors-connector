package org.mule.extension.vectors.internal.connection.embeddings.mistralai;

import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
