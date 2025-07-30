package org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface;

import org.mule.extension.vectors.internal.connection.provider.embeddings.BaseModelConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class HuggingFaceModelConnectionParameters extends BaseModelConnectionParameters {

  @Parameter
  @Password
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  @Example("<your-api-key>")
  @Summary("This embedding model is supported as beta. Please refer to the product documentation.")
  private String apiKey;

  public String getApiKey() {
    return apiKey;
  }

}
