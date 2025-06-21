package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.provider.FileTypeProvider;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.values.OfValues;

public class FileParameters {

  @Parameter
  @Alias("contextPath")
  @DisplayName("Context Path")
  @Summary("The context path.")
  @Placement(order = 1)
  @Expression(ExpressionSupport.SUPPORTED)
  private String contextPath;

  public String getContextPath() {
    return contextPath;
  }
}
