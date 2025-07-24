package org.mule.extension.vectors.internal.connection.provider.embeddings.openai;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class OpenAIModelConnectionParameters extends BaseModelConnectionParameters {

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  @Example("<your-api-key>")
  private String apiKey;

  public String getApiKey() {
    return apiKey;
  }
}
