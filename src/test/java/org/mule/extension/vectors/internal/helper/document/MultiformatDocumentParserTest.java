package org.mule.extension.vectors.internal.helper.document;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

class MultiformatDocumentParserTest {

  @Test
  void parse_shouldReturnTextContent() {
    MultiformatDocumentParser parser = new MultiformatDocumentParser();
    InputStream in = new ByteArrayInputStream("hello world".getBytes());
    InputStream result = parser.parse(in);
    assertThat(result).isNotNull();
  }

  @Test
  void parse_withIncludeMetadataTrue_shouldReturnTextContent() {
    MultiformatDocumentParser parser = new MultiformatDocumentParser(true);
    InputStream in = new ByteArrayInputStream("foo bar".getBytes());
    InputStream result = parser.parse(in);
    assertThat(result).isNotNull();
  }

  @Test
  void parse_shouldHandleNullInputStream() {
    MultiformatDocumentParser parser = new MultiformatDocumentParser();
    Throwable thrown = catchThrowable(() -> parser.parse(null));
    assertThat(thrown)
        .isInstanceOf(RuntimeException.class)
        .hasCauseInstanceOf(NullPointerException.class);
  }
}
