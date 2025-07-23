package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.extension.vectors.internal.constant.Constants;
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
@DisplayName(Constants.TRANSORMER_PARSER_MULTIFORMAT)
public class MultiformatDocumentParserParameters implements DocumentParserParameters{

  @Parameter
  @Alias("includeMetadata")
  @DisplayName("Include metadata")
  @Summary("Whether to include metadata in the parsed document.")
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional(defaultValue = "false")
  private boolean includeMetadata;

  @Override
  public String getName() {
    return Constants.TRANSORMER_PARSER_MULTIFORMAT;
  }

  @Override
  public DocumentParser getDocumentParser() {
    return new MultiformatDocumentParser(includeMetadata);
  }

  public boolean isIncludeMetadata() {
    return includeMetadata;
  }
}
