package org.mule.extension.vectors.internal.connection.embeddings.openai;

import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("openAI")
@DisplayName("OpenAI")
public class OpenAIModelConnectionProvider extends BaseModelConnectionProvider {

 
  @ParameterGroup(name = CONNECTION)
  private OpenAIModelConnectionParameters openAIModelConnectionParameters;


  @Override
  public BaseModelConnection connect() throws ConnectionException {

    OpenAIModelConnection openAIModelConnection = new OpenAIModelConnection(
        openAIModelConnectionParameters.getApiKey(),
        openAIModelConnectionParameters.getTotalTimeout(),
        getHttpClient());
    return openAIModelConnection;
  }

}
