package org.mule.extension.vectors.internal.connection.store.milvus;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.extension.vectors.internal.helper.provider.MilvusConsistencyLevelProvider;
import org.mule.extension.vectors.internal.helper.provider.MilvusIndexTypeProvider;
import org.mule.extension.vectors.internal.helper.provider.MilvusMetricTypeProvider;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.values.OfValues;

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
  @OfValues(MilvusIndexTypeProvider.class)
  @Optional(defaultValue = "FLAT")
  private String indexType;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2, tab = Placement.ADVANCED_TAB)
  @OfValues(MilvusMetricTypeProvider.class)
  @Optional(defaultValue = "COSINE")
  private String metricType;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3, tab = Placement.ADVANCED_TAB)
  @OfValues(MilvusConsistencyLevelProvider.class)
  @Optional(defaultValue = "EVENTUALLY")
  private String consistencyLevel;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 4, tab = Placement.ADVANCED_TAB)
  @Optional(defaultValue = "true")
  private boolean autoFlushOnInsert;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 5, tab = Placement.ADVANCED_TAB)
  @Optional(defaultValue = "id")
  private String idFieldName;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 6, tab = Placement.ADVANCED_TAB)
  @Optional(defaultValue = "text")
  private String textFieldName;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 7, tab = Placement.ADVANCED_TAB)
  @Optional(defaultValue = "metadata")
  private String metadataFieldName;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 8, tab = Placement.ADVANCED_TAB)
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

  public String getIndexType() {
    return indexType;
  }

  public String getMetricType() {
    return metricType;
  }

  public String getConsistencyLevel() {
    return consistencyLevel;
  }

  public boolean isAutoFlushOnInsert() {
    return autoFlushOnInsert;
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
