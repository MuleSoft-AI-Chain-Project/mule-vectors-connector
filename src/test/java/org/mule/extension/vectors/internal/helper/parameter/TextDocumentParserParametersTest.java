package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.api.helper.document.DocumentParser;
import org.mule.extension.vectors.api.helper.document.TextDocumentParser;
import org.mule.extension.vectors.api.parameter.TextDocumentParserParameters;
import org.mule.extension.vectors.internal.constant.Constants;

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

  @Test
  void equals_sameObject_shouldReturnTrue() {
    TextDocumentParserParameters params = new TextDocumentParserParameters();
    assertThat(params.equals(params)).isTrue();
  }

  @Test
  void equals_null_shouldReturnFalse() {
    TextDocumentParserParameters params = new TextDocumentParserParameters();
    assertThat(params.equals(null)).isFalse();
  }

  @Test
  void equals_differentClass_shouldReturnFalse() {
    TextDocumentParserParameters params = new TextDocumentParserParameters();
    assertThat(params.equals("not a params object")).isFalse();
  }

  @Test
  void equals_sameValues_shouldReturnTrue() {
    TextDocumentParserParameters params1 = new TextDocumentParserParameters();
    TextDocumentParserParameters params2 = new TextDocumentParserParameters();
    assertThat(params1).isEqualTo(params2);
  }

  @Test
  void hashCode_sameValues_shouldBeEqual() {
    TextDocumentParserParameters params1 = new TextDocumentParserParameters();
    TextDocumentParserParameters params2 = new TextDocumentParserParameters();
    assertThat(params1.hashCode()).isEqualTo(params2.hashCode());
  }

  @Test
  void hashCode_shouldBeConsistent() {
    TextDocumentParserParameters params = new TextDocumentParserParameters();
    int hash1 = params.hashCode();
    int hash2 = params.hashCode();
    assertThat(hash1).isEqualTo(hash2);
  }
}
