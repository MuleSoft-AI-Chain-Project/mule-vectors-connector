package org.mule.extension.vectors.internal.store.pinecone;

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

@Alias("pinecone")
@DisplayName("Pinecone")
public class PineconeStoreConfiguration implements BaseStoreConfiguration {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("AWS")
  private String cloud;

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2)
  @Example("us-east-1")
  private String region;

  @Parameter
  @Password
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 3)
  @Example("<your-apikey>")
  private String apiKey;

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_PINECONE;
  }

  public String getCloud() {
    return cloud;
  }

  public String getRegion() {
    return region;
  }

  public String getApiKey() {
    return apiKey;
  }
}
