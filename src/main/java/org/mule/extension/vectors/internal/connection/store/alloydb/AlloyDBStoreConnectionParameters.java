package org.mule.extension.vectors.internal.connection.store.alloydb;

import org.mule.extension.vectors.internal.connection.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.Optional;

public class AlloyDBStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1, tab = "Application Default Credentials (ADC)")
  @Example("ehc-apache-915459")
  private String projectId;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2, tab = "Application Default Credentials (ADC)")
  @Example("us-east1")
  private String region;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3, tab = "Application Default Credentials (ADC)")
  @Example("mac-alloydb-cluster")
  private String cluster;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 4, tab = "Application Default Credentials (ADC)")
  @Example("mac-alloydb-cluster-primary")
  private String instance;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 5, tab = "Application Default Credentials (ADC)")
  @Example("mac-vectors-dev-01@ehc-tbolis-915459.iam.gserviceaccount.com")
  private String iamAccountEmail;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 6, tab = "Private/Public IP")
  @Example("localhost")
  private String host;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 7, tab = "Private/Public IP")
  @Optional(defaultValue = "public")
  private String ipType;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 8, tab = "Private/Public IP")
  @Optional(defaultValue = "5432")
  private int port;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Example("postgres")
  private String database;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Example("postgres")
  private String user;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  private String password;

  public String getProjectId() {
    return projectId;
  }
  
  public String getRegion() {
    return region;
  }
  
  public String getCluster() {
    return cluster;
  }
  
  public String getInstance() {
    return instance;
  }
  
  public String getIamAccountEmail() {
    return iamAccountEmail;
  }
  
  public String getIpType() {
    return ipType;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getDatabase() {
    return database;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
