package org.mule.extension.vectors.internal.connection.provider.store.chroma;

import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnectionParameters;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class ChromaStoreConnectionParameters extends BaseStoreConnectionParameters {

  @Parameter
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  @Example("http://localhost:8000")
  @Summary("This is vector store is supported as beta. Please refer to the product documentation.")
  private String url;

  public String getUrl() {
    return url;
  }
}
