package org.mule.extension.vectors.internal.store.opensearch;

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

@Alias("openSearch")
@DisplayName("OpenSearch")
public class OpenSearchStoreConfiguration implements BaseStoreConfiguration {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("https://search-mydomain-xyz123456.us-west-1.es.amazonaws.com")
  private String url;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2)
  @Example("<your-username>")
  private String userName;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3)
  @Example("<your-password>")
  private String password;

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_OPENSEARCH;
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
