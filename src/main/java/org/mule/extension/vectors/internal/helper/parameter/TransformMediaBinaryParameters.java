package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.provider.MediaTypeProvider;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.values.OfValues;

import java.io.InputStream;

public class TransformMediaBinaryParameters {

  @Parameter
  @Alias("binary")
  @DisplayName("Binary")
  @Summary("The media binary.")
  @Placement(order = 1)
  @Expression(ExpressionSupport.SUPPORTED)
  private @Content InputStream binaryInputStream;

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

  public InputStream getBinaryInputStream() {
    return binaryInputStream;
  }

  public MediaProcessorParameters getMediaProcessorParameters() {
    return mediaProcessorParameters;
  }

  @Override
  public String toString() {
    return "MediaParameters{" +
        "mediaType='" + mediaType + '\'' +
        ", mediaProcessorParameters=" + mediaProcessorParameters +
        '}';
  }
}
