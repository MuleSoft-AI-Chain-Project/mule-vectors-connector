package org.mule.extension.vectors.internal.connection.store.opensearch;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@ExclusiveOptionals(isOneRequired = true)
public class OpenSearchStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement
  @Example("https://search-mydomain-xyz123456.us-west-1.es.amazonaws.com")
  private String url;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement
  @Example("admin")
  @Optional
  private String user;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement
  private String password;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement
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
