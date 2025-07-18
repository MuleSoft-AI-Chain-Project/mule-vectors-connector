package org.mule.extension.vectors.internal.connection.provider.embeddings.openai;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

@Alias("openAI")
@DisplayName("OpenAI")
public class OpenAIModelConnectionProvider extends BaseModelConnectionProvider {

 
  @ParameterGroup(name = CONNECTION)
  private OpenAIModelConnectionParameters openAIModelConnectionParameters;


  @Override
  public BaseModelConnection connect() throws ConnectionException {

    OpenAIModelConnection openAIModelConnection = new OpenAIModelConnection(
        openAIModelConnectionParameters.getApiKey(),
        openAIModelConnectionParameters.getTimeout(),
        getHttpClient());
    return openAIModelConnection;
  }

}
