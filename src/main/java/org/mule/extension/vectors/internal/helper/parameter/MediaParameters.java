package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.provider.MediaTypeProvider;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.values.OfValues;

public class MediaParameters {

  @Parameter
  @Alias("contextPath")
  @DisplayName("Context Path")
  @Summary("The context path.")
  @Placement(order = 1)
  @Expression(ExpressionSupport.SUPPORTED)
  private String contextPath;

  @Parameter
  @Alias("mediaType")
  @DisplayName("Media Type")
  @Summary("The supported types of media.")
  @Placement(order = 2)
  @Expression(ExpressionSupport.SUPPORTED)
  @OfValues(MediaTypeProvider.class)
  @Optional(defaultValue = Constants.MEDIA_TYPE_IMAGE)
  private String mediaType;

  @Parameter
  @Alias("mediaProcessorParameters")
  @DisplayName("Processor Settings")
  @Summary("The context path.")
  @Placement(order = 3)
  @Expression(ExpressionSupport.NOT_SUPPORTED)
  private MediaProcessorParameters mediaProcessorParameters = new ImageProcessorParameters();


  public String getMediaType() {
    return mediaType;
  }

  public String getContextPath() {
    return contextPath;
  }

  public MediaProcessorParameters getMediaProcessorParameters() {
    return mediaProcessorParameters;
  }

  @Override
  public String toString() {
    return "MediaParameters{" +
        "contextPath='" + contextPath + '\'' +
        ", mediaType='" + mediaType + '\'' +
        ", mediaProcessorParameters=" + mediaProcessorParameters +
        '}';
  }
}
