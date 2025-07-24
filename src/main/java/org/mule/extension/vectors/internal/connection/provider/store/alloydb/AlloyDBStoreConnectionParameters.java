package org.mule.extension.vectors.internal.connection.provider.store.alloydb;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

public class AlloyDBStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  @Example("ehc-apache-915459")
  private String projectId;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 2)
  @Example("us-east1")
  private String region;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 3)
  @Example("mac-alloydb-cluster")
  private String cluster;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 4)
  @Example("mac-alloydb-cluster-primary")
  private String instance;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 5)
  @Example("mac-vectors-dev-01@ehc-tbolis-915459.iam.gserviceaccount.com")
  private String iamAccountEmail;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 6)
  @Example("localhost")
  private String host;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 7)
  @Optional(defaultValue = "public")
  private String ipType;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 8)
  @Optional(defaultValue = "5432")
  private int port;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 9)
  @Example("postgres")
  private String database;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 10)
  @Example("postgres")
  private String user;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 11)
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
