package org.mule.extension.vectors.internal.connection.provider.store.weaviate;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
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
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  @Example("https")
  private String scheme;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 2)
  @Example("localhost")
  private String host;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Example("8181")
  @Optional
  private Integer port;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional(defaultValue = "false")
  private boolean securedGrpc;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional
  private Integer grpcPort;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional(defaultValue = "false")
  private boolean useGrpcForInserts;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 3)
  @Password
  private String apiKey;

  @Parameter
  @DisplayName("Avoid duplicates")
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional(defaultValue = "true")
  private boolean avoidDups;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
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

  public boolean isSecuredGrpc() { return securedGrpc; }

  public Integer getGrpcPort() { return grpcPort; }

  public boolean isUseGrpcForInserts() { return useGrpcForInserts; }

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
