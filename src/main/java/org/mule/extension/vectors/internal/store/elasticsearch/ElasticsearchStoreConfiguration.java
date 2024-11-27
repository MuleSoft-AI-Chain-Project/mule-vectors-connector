package org.mule.extension.vectors.internal.store.elasticsearch;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.store.BaseStoreConfiguration;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("elasticsearch")
@DisplayName("Elasticsearch")
public class ElasticsearchStoreConfiguration implements BaseStoreConfiguration {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  private String url;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2)
  private String userName;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3)
  private String password;

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_ELASTICSEARCH;
  }

  public String getUrl() {
    return url;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }
}
