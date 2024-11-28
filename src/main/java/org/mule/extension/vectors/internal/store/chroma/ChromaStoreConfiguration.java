package org.mule.extension.vectors.internal.store.chroma;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.store.BaseStoreConfiguration;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

@Alias("chroma")
@DisplayName("Chroma")
public class ChromaStoreConfiguration implements BaseStoreConfiguration {

  @Parameter
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 1)
  @Example("http://localhost:8000")
  private String url;

  @Override
  public String getVectorStore() {
    return Constants.VECTOR_STORE_CHROMA;
  }

  public String getUrl() {
    return url;
  }
}
