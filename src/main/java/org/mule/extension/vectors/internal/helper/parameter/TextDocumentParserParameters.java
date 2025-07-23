package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.document.DocumentParser;
import org.mule.extension.vectors.internal.helper.document.TextDocumentParser;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

@Alias("textDocumentParserParameters")
@DisplayName(Constants.TRANSORMER_PARSER_TEXTFORMAT)
public class TextDocumentParserParameters implements DocumentParserParameters {

  @Parameter
  @Alias("charset")
  @DisplayName("Charset")
  @Summary("The charset for the parsed text.")
  @Expression(ExpressionSupport.SUPPORTED)
  @Optional(defaultValue = "UTF_8")
  private String charset;

  public String getCharset() {
    return charset;
  }

  @Override
  public String getName() {
    return Constants.TRANSORMER_PARSER_TEXTFORMAT;
  }

  @Override
  public DocumentParser getDocumentParser() {
    return new TextDocumentParser(this.charset);
  }
}
