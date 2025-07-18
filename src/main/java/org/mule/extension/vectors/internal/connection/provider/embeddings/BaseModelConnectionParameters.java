package org.mule.extension.vectors.internal.connection.provider.embeddings;

import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public abstract class BaseModelConnectionParameters {
  @Parameter
  @DisplayName("Timeout")
  @Summary("Timeout for the operation in milliseconds")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(order = 2, tab = Placement.ADVANCED_TAB)
  @Example("60000")
  @Optional(defaultValue = "60000")
  @ConfigOverride
  public long totalTimeout;

  public long getTimeout() {
    return this.totalTimeout;
  }
}
