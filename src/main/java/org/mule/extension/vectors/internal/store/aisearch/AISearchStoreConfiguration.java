package org.mule.extension.vectors.internal.store.aisearch;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.store.BaseStoreConfiguration;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import javax.annotation.processing.SupportedOptions;

@Alias("aiSearch")
@DisplayName("AI Search")
public class AISearchStoreConfiguration implements BaseStoreConfiguration {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("https://<resource-name>.search.windows.net")
  private String url;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2)
  @Example("<your-api-key>")
  private String apiKey;

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_AI_SEARCH;
  }

  public String getUrl() {
    return url;
  }

  public String getApiKey() {
    return apiKey;
  }
}
