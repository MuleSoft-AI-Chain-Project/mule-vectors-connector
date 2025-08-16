package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.api.parameter.TextDocumentParserParameters;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.document.DocumentParser;
import org.mule.extension.vectors.internal.helper.document.TextDocumentParser;

import org.junit.jupiter.api.Test;

class TextDocumentParserParametersTest {

  @Test
  void getName_shouldReturnCorrectName() {
    TextDocumentParserParameters params = new TextDocumentParserParameters();
    assertThat(params.getName()).isEqualTo(Constants.TRANSORMER_PARSER_TEXTFORMAT);
  }

  @Test
  void getDocumentParser_returnsTextDocumentParser() {
    TextDocumentParserParameters params = new TextDocumentParserParameters();
    DocumentParser parser = params.getDocumentParser();
    assertThat(parser).isInstanceOf(TextDocumentParser.class);
  }
}
