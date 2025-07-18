package org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnection;
import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;


@Alias("azureAIVision")
@DisplayName("Azure AI Vision")
public class AzureAIVisionModelConnectionProvider extends BaseModelConnectionProvider  {

  @ParameterGroup(name = CONNECTION)
  private AzureAIVisionModelConnectionParameters azureAIVisionModelConnectionParameters;


  @Override
  public BaseModelConnection connect() throws ConnectionException {

      AzureAIVisionModelConnection azureAIVisionModelConnection =
          new AzureAIVisionModelConnection(azureAIVisionModelConnectionParameters.getEndpoint(),
                                         azureAIVisionModelConnectionParameters.getApiKey(),
                                         azureAIVisionModelConnectionParameters.getApiVersion(),
                                         azureAIVisionModelConnectionParameters.getTimeout(),
                                           getHttpClient());

      return azureAIVisionModelConnection;

  }

}

