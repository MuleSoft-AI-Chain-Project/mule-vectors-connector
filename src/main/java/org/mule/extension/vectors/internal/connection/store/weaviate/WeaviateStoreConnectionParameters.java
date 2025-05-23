package org.mule.extension.vectors.internal.connection.store.weaviate;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class WeaviateStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("https")
  private String scheme;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2)
  @Example("localhost")
  private String host;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3)
  @Example("8181")
  @Optional
  private Integer port;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 4)
  @Password
  private String apiKey;

  @Parameter
  @DisplayName("Avoid duplicates")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 5)
  @Optional(defaultValue = "true")
  private boolean avoidDups;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 6)
  @Optional(defaultValue = "ALL")
  private String consistencyLevel;

  public String getScheme() {
    return scheme;
  }

  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public String getApiKey() {
    return apiKey;
  }

  public boolean isAvoidDups() {
    return avoidDups;
  }

  public String getConsistencyLevel() {
    return consistencyLevel;
  }
}
