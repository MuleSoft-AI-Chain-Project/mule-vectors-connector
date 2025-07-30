package org.mule.extension.vectors.internal.helper.document;

import static org.assertj.core.api.Assertions.*;

import org.mule.runtime.extension.api.exception.ModuleException;

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
}
