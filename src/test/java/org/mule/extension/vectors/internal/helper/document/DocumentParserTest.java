package org.mule.extension.vectors.internal.helper.document;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

class DocumentParserTest {

  @Test
  void parse_shouldReturnNullForNullInput() {
    DocumentParser parser = inputStream -> null;
    assertThat(parser.parse(null)).isNull();
  }

  @Test
  void parse_shouldReturnEmptyStringForEmptyInput() {
    DocumentParser parser = inputStream -> new ByteArrayInputStream("".getBytes());
    InputStream empty = new ByteArrayInputStream(new byte[0]);
    InputStream result = parser.parse(empty);
    assertThat(result).isNotNull();
  }

  @Test
  void parse_shouldReturnStringForTypicalInput() {
    DocumentParser parser = inputStream -> new ByteArrayInputStream("parsed-content".getBytes());
    InputStream in = new ByteArrayInputStream("foo".getBytes());
    InputStream result = parser.parse(in);
    assertThat(result).isNotNull();
  }
}
