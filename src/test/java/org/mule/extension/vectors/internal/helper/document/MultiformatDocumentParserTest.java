package org.mule.extension.vectors.internal.helper.document;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.api.helper.document.MultiformatDocumentParser;

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

  @Test
  void equals_sameObject_shouldReturnTrue() {
    MultiformatDocumentParser parser = new MultiformatDocumentParser();
    assertThat(parser).isEqualTo(parser);
  }

  @Test
  void equals_sameIncludeMetadata_shouldReturnTrue() {
    MultiformatDocumentParser p1 = new MultiformatDocumentParser(false);
    MultiformatDocumentParser p2 = new MultiformatDocumentParser(false);
    assertThat(p1).isEqualTo(p2);
  }

  @Test
  void equals_differentIncludeMetadata_shouldReturnFalse() {
    MultiformatDocumentParser p1 = new MultiformatDocumentParser(false);
    MultiformatDocumentParser p2 = new MultiformatDocumentParser(true);
    assertThat(p1).isNotEqualTo(p2);
  }

  @Test
  void equals_null_shouldReturnFalse() {
    MultiformatDocumentParser parser = new MultiformatDocumentParser();
    assertThat(parser).isNotEqualTo(null);
  }

  @Test
  void equals_differentClass_shouldReturnFalse() {
    MultiformatDocumentParser parser = new MultiformatDocumentParser();
    assertThat(parser).isNotEqualTo("string");
  }

  @Test
  void hashCode_sameIncludeMetadata_shouldBeEqual() {
    MultiformatDocumentParser p1 = new MultiformatDocumentParser(true);
    MultiformatDocumentParser p2 = new MultiformatDocumentParser(true);
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  void hashCode_differentIncludeMetadata_shouldDiffer() {
    MultiformatDocumentParser p1 = new MultiformatDocumentParser(false);
    MultiformatDocumentParser p2 = new MultiformatDocumentParser(true);
    assertThat(p1.hashCode()).isNotEqualTo(p2.hashCode());
  }
}
