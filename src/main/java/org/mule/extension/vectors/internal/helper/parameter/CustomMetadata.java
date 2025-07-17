package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.HashMap;

public class CustomMetadata {

  @Parameter
  @Alias("metadataEntries")
  @DisplayName("Metadata entries")
  @Summary("Custom metadata key-value pairs to be added to the vector store")
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional
  HashMap<String, String> metadataEntries;

  public HashMap<String, String> getMetadataEntries() {
    return metadataEntries;
  }
}
