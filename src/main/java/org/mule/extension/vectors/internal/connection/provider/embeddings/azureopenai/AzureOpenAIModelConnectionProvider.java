package org.mule.extension.vectors.internal.connection.provider.embeddings.azureopenai;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

@Alias("azureOpenAI")
@DisplayName("Azure OpenAI")
public class AzureOpenAIModelConnectionProvider  extends BaseModelConnectionProvider  {


  @ParameterGroup(name = CONNECTION)
  private AzureOpenAIModelConnectionParameters azureOpenAIModelConnectionParameters;

 
  @Override
  public BaseModelConnection connect() throws ConnectionException {


      AzureOpenAIModelConnection azureOpenAIModelConnection =
          new AzureOpenAIModelConnection(azureOpenAIModelConnectionParameters.getEndpoint(),
                                         azureOpenAIModelConnectionParameters.getApiKey(),
                                         azureOpenAIModelConnectionParameters.getApiVersion(),
                                         azureOpenAIModelConnectionParameters.getTimeout(),
                                         getHttpClient());
      return azureOpenAIModelConnection;
  }
}
