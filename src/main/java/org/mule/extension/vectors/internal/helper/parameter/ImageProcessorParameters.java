package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.extension.vectors.internal.helper.media.ImageProcessor;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

@Alias("imageProcessorParameters")
@DisplayName("Image Processor Settings")
public class ImageProcessorParameters extends MediaProcessorParameters {

  @Parameter
  @Alias("targetWidth")
  @DisplayName("Target Width (pixels)")
  @Summary("The target width for media.")
  @Placement(order = 1)
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional(defaultValue = "512")
  private int targetWidth;

  @Parameter
  @Alias("targetHeight")
  @DisplayName("Target Height (pixels)")
  @Summary("The target height for media.")
  @Placement(order = 2)
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional(defaultValue = "512")
  private int targetHeight;

  @Parameter
  @Alias("compressionQuality")
  @DisplayName("Compression Quality")
  @Summary("The compression quality for media (between 0.0 and 1.0, where 1.0 is highest quality).")
  @Placement(order = 3)
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional(defaultValue = "1")
  private float compressionQuality;

  @Parameter
  @Alias("scaleStrategy")
  @DisplayName("Scale Strategy")
  @Summary("The scaling strategy ('fit' with black padding, fill with crop or strech).")
  @Placement(order = 4)
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional(defaultValue = "FIT")
  private ImageProcessor.ScaleStrategy scaleStrategy;

  public int getTargetWidth() {
    return targetWidth;
  }

  public int getTargetHeight() {
    return targetHeight;
  }

  public float getCompressionQuality() {
    return compressionQuality;
  }

  public ImageProcessor.ScaleStrategy getScaleStrategy() {
    return scaleStrategy;
  }

  @Override
  public String toString() {
    return "ImageProcessorParameters{" +
        "targetWidth=" + targetWidth +
        ", targetHeight=" + targetHeight +
        ", compressionQuality=" + compressionQuality +
        ", scaleStrategy=" + scaleStrategy +
        '}';
  }
}
