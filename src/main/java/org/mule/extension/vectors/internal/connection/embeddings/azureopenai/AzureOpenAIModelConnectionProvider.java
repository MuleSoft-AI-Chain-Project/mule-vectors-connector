package org.mule.extension.vectors.internal.connection.embeddings.azureopenai;

import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Alias("azureOpenAI")
@DisplayName("Azure OpenAI")
public class AzureOpenAIModelConnectionProvider  extends BaseModelConnectionProvider  {

  private static final Logger LOGGER = LoggerFactory.getLogger(AzureOpenAIModelConnectionProvider.class);


  @ParameterGroup(name = CONNECTION)
  private AzureOpenAIModelConnectionParameters azureOpenAIModelConnectionParameters;

 
  @Override
  public BaseModelConnection connect() throws ConnectionException {


      AzureOpenAIModelConnection azureOpenAIModelConnection =
          new AzureOpenAIModelConnection(azureOpenAIModelConnectionParameters.getEndpoint(),
                                         azureOpenAIModelConnectionParameters.getApiKey(),
                                         azureOpenAIModelConnectionParameters.getApiVersion(),
                                         azureOpenAIModelConnectionParameters.getTotalTimeout(),
                                         getHttpClient());
      return azureOpenAIModelConnection;
  }
}
