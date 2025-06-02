package org.mule.extension.vectors.internal.connection.store.milvus;

import io.milvus.param.IndexType;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class MilvusStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("localhost")
  private String host;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2)
  @Example("19530")
  private Integer port;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3)
  @Optional
  private String token;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 4)
  @Optional
  private String username;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 4)
  @Optional
  private String password;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 5)
  @Optional
  private String databaseName;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1, tab = Placement.ADVANCED_TAB)
  @Optional(defaultValue = "id")
  private String idFieldName;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1, tab = Placement.ADVANCED_TAB)
  @Optional(defaultValue = "text")
  private String textFieldName;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1, tab = Placement.ADVANCED_TAB)
  @Optional(defaultValue = "metadata")
  private String metadataFieldName;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1, tab = Placement.ADVANCED_TAB)
  @Optional(defaultValue = "vector")
  private String vectorFieldName;

  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public String getToken() {
    return token;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public String getIdFieldName() {
    return idFieldName;
  }

  public String getTextFieldName() {
    return textFieldName;
  }

  public String getMetadataFieldName() {
    return metadataFieldName;
  }

  public String getVectorFieldName() {
    return vectorFieldName;
  }
}
