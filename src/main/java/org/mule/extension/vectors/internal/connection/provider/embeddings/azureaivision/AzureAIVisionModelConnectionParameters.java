package org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class AzureAIVisionModelConnectionParameters extends BaseModelConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  @Example("https://<resource-name>.cognitiveservices.azure.com")
  @Summary("This embedding model is supported as beta. Please refer to the product documentation.")
  private String endpoint;

  @Parameter
  @Password
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 2)
  @Example("<your-api-key>")
  private String apiKey;

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 3)
  @Example("2023-04-01-preview")
  private String apiVersion = "2023-04-01-preview";

  public String getEndpoint() {
    return endpoint;
  }

  public String getApiKey() {
    return apiKey;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public long getTotalTimeout() {
    return this.getTimeout();
  }
}

