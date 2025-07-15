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

public class EmbeddingMediaBinaryParameters {

  @Parameter
  @Alias("binary")
  @DisplayName("Binary")
  @Summary("The media binary.")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 1)
  private @Content InputStream binaryInputStream;

  @Parameter
  @Alias("label")
  @DisplayName("Media Label")
  @Summary("Short text describing the image. " +
      "Not all models allow to generate embedding for a combination of label and image.")
  @Example("An image of a sunset")
  @Expression(ExpressionSupport.SUPPORTED)
  @Placement(tab = Placement.DEFAULT_TAB, order = 2)
  private @Content String label;

  @Parameter
  @Alias("mediaType")
  @DisplayName("Media Type")
  @Summary("The supported types of media.")
  @Expression(ExpressionSupport.SUPPORTED)
  @OfValues(MediaTypeProvider.class)
  @Optional(defaultValue = Constants.MEDIA_TYPE_IMAGE)
  @Placement(tab = Placement.DEFAULT_TAB, order = 3)
  private String mediaType;

  public String getMediaType() {
    return mediaType;
  }

  public InputStream getBinaryInputStream() { return binaryInputStream;}

  public String getLabel() { return label; }

  @Override
  public String toString() {
    return "MediaParameters{" +
        "mediaType='" + mediaType + '\'' +
        '}';
  }
}
