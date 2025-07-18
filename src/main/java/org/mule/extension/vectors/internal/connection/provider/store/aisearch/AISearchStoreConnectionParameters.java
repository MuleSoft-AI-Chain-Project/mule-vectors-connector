package org.mule.extension.vectors.internal.connection.provider.store.aisearch;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class AISearchStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  @Example("https://<resource-name>.search.windows.net")
  private String url;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 2)
  @Example("<your-api-key>")
  private String apiKey;

  public String getUrl() {
    return url;
  }

  public String getApiKey() {
    return apiKey;
  }
}
