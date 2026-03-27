package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.assertThat;

import org.mule.extension.vectors.api.parameter.DocumentParserParameters;
import org.mule.extension.vectors.internal.helper.document.DocumentParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

class DocumentParserParametersTest {

  @Test
  void getDocumentParser_returnsExpected() {
    DocumentParser expected = new DocumentParser() {

      @Override
      public InputStream parse(java.io.InputStream is) {
        return new ByteArrayInputStream("dummy".getBytes());
      }
    };
    DocumentParserParameters params = new DocumentParserParameters() {

      @Override
      public String getName() {
        return "test";
      }

      @Override
      public DocumentParser getDocumentParser() {
        return expected;
      }
    };
    assertThat(params.getDocumentParser()).isSameAs(expected);
  }
}
