package org.mule.extension.vectors.internal.connection.provider.store.elasticsearch;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@ExclusiveOptionals(isOneRequired = true)
public class ElasticsearchStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  @Example("http://localhost:9200")
  private String url;

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 2)
  @Example("elasticsearch")
  @Optional
  private String user;

  @Parameter
  @Password
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 3)
  private String password;

  @Parameter
  @Password
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Optional
  private String apiKey;

  public String getUrl() {
    return url;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getApiKey() {
    return apiKey;
  }
}
