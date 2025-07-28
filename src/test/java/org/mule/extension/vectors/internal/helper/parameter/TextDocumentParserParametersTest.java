package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.api.parameter.TextDocumentParserParameters;
import org.mule.extension.vectors.internal.helper.document.DocumentParser;
import org.mule.extension.vectors.internal.helper.document.TextDocumentParser;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class TextDocumentParserParametersTest {

  @Test
  void getCharset_shouldDefaultToNull() {
    TextDocumentParserParameters params = new TextDocumentParserParameters();
    assertThat(params.getCharset()).isNull();
  }

  @Test
  void getCharset_shouldReflectSetValue() throws Exception {
    TextDocumentParserParameters params = new TextDocumentParserParameters();
    Field field = params.getClass().getDeclaredField("charset");
    field.setAccessible(true);
    field.set(params, "ISO-8859-1");
    assertThat(params.getCharset()).isEqualTo("ISO-8859-1");
  }

  @Test
  void getDocumentParser_returnsTextDocumentParserWithCorrectCharset() throws Exception {
    TextDocumentParserParameters params = new TextDocumentParserParameters();
    Field field = params.getClass().getDeclaredField("charset");
    field.setAccessible(true);
    field.set(params, "UTF-8");
    DocumentParser parser = params.getDocumentParser();
    assertThat(parser).isInstanceOf(TextDocumentParser.class);
    java.lang.reflect.Field charsetField = TextDocumentParser.class.getDeclaredField("charset");
    charsetField.setAccessible(true);
    Object charsetObj = charsetField.get(parser);
    assertThat(charsetObj.toString()).contains("UTF-8");
  }
}
