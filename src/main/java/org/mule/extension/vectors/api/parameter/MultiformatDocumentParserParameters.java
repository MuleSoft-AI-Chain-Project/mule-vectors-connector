package org.mule.extension.vectors.api.parameter;

import org.mule.extension.vectors.internal.helper.document.DocumentParser;
import org.mule.extension.vectors.internal.helper.document.MultiformatDocumentParser;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

@Alias("multiformatDocumentParserParameters")
@DisplayName("Multiformat document parse")
public class MultiformatDocumentParserParameters implements DocumentParserParameters {

  @Parameter
  @Alias("includeMetadata")
  @DisplayName("Include metadata")
  @Summary("Whether to include metadata in the parsed document.")
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional(defaultValue = "false")
  private boolean includeMetadata;

  @Override
  public String getName() {
    return "Multiformat document parse";
  }

  @Override
  public DocumentParser getDocumentParser() {
    return new MultiformatDocumentParser(includeMetadata);
  }

  public boolean isIncludeMetadata() {
    return includeMetadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MultiformatDocumentParserParameters that = (MultiformatDocumentParserParameters) o;
    return includeMetadata == that.includeMetadata;
  }

  @Override
  public int hashCode() {
    return Boolean.hashCode(includeMetadata);
  }
}
