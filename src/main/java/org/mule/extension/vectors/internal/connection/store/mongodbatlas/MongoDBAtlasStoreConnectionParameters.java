package org.mule.extension.vectors.internal.connection.store.mongodbatlas;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class MongoDBAtlasStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement
  @Example("localhost")
  private String host;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement
  @Example("27017")
  @Optional
  private Integer port;

  @Parameter
  @DisplayName("Username")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement
  @Example("user")
  @Optional
  private String user;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement
  private String password;

  @Parameter
  @DisplayName("Database Name")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement
  @Example("default")
  @Optional
  private String database;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement
  @Example("directConnection=true")
  @Optional
  private String options;

  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getDatabase() {
    return database;
  }

  public String getOptions() {
    return options;
  }
}
