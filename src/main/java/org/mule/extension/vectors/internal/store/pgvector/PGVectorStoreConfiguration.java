package org.mule.extension.vectors.internal.store.pgvector;

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

@Alias("pgVector")
@DisplayName("PGVector")
public class PGVectorStoreConfiguration implements BaseStoreConfiguration {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("localhost")
  private String host;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2)
  @Example("5432")
  private int port;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3)
  @Example("default")
  private String database;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 4)
  @Example("<your-username>")
  private String userName;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 5)
  @Example("<your-password>")
  private String password;

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_PGVECTOR;
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

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }
}
