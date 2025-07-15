package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

public class SegmentationParameters {

  @Parameter
  @Alias("maxSegmentSizeInChar")
  @DisplayName("Max Segment Size (Characters)")
  @Summary("Maximum size of a segment in characters.")
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional
  int maxSegmentSizeInChars;

  @Parameter
  @Alias("maxOverlapSizeInChars")
  @DisplayName("Max Overlap Size (Characters)")
  @Summary("Maximum overlap between segments in characters.")
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional
  int maxOverlapSizeInChars;

  public int getMaxSegmentSizeInChars() {
    return maxSegmentSizeInChars;
  }

  public int getMaxOverlapSizeInChars() {
    return maxOverlapSizeInChars;
  }
}
