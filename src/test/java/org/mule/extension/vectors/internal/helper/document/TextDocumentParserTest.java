package org.mule.extension.vectors.internal.helper.document;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.internal.helper.document.TextDocumentParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class TextDocumentParserTest {

  @Test
  void parse_shouldReturnTextContent() {
    TextDocumentParser parser = new TextDocumentParser();
    InputStream in = new ByteArrayInputStream("hello world".getBytes());
    InputStream result = parser.parse(in);
    assertThat(result).isNotNull();
  }

  @Test
  void parse_shouldReturnEmptyStreamForEmptyInput() {
    TextDocumentParser parser = new TextDocumentParser();
    InputStream in = new ByteArrayInputStream(new byte[0]);
    InputStream result = parser.parse(in);
    assertThat(result).isNotNull();
    assertThat(result).isSameAs(in);
  }

  @Test
  void parse_shouldReturnNullForNullInputStream() {
    TextDocumentParser parser = new TextDocumentParser();
    InputStream result = parser.parse(null);
    assertThat(result).isNull();
  }

  @Test
  void equals_sameObject_shouldReturnTrue() {
    TextDocumentParser parser = new TextDocumentParser();
    assertThat(parser).isEqualTo(parser);
  }

  @Test
  void equals_sameType_shouldReturnTrue() {
    TextDocumentParser p1 = new TextDocumentParser();
    TextDocumentParser p2 = new TextDocumentParser();
    assertThat(p1).isEqualTo(p2);
  }

  @Test
  void equals_null_shouldReturnFalse() {
    TextDocumentParser parser = new TextDocumentParser();
    assertThat(parser).isNotEqualTo(null);
  }

  @Test
  void equals_differentClass_shouldReturnFalse() {
    TextDocumentParser parser = new TextDocumentParser();
    assertThat(parser).isNotEqualTo("string");
  }

  @Test
  void hashCode_sameType_shouldBeEqual() {
    TextDocumentParser p1 = new TextDocumentParser();
    TextDocumentParser p2 = new TextDocumentParser();
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }
}
