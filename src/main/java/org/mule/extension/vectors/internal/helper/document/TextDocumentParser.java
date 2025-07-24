package org.mule.extension.vectors.internal.helper.document;

import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TextDocumentParser implements DocumentParser {

  private Charset charset;

  public TextDocumentParser() {
    this.charset = StandardCharsets.UTF_8;
  }

  public TextDocumentParser(String charset) {

    this.charset = charset != null ? Charset.availableCharsets().get(charset) : StandardCharsets.UTF_8;
  }

  @Override
  public String parse(InputStream inputStream) {

    try {
      String text = new String(inputStream.readAllBytes(), this.charset);
      if (text.isBlank()) {
        throw new ModuleException("", MuleVectorsErrorType.TRANSFORM_DOCUMENT_PARSING_FAILURE);
      } else {
        return text;
      }
    } catch (ModuleException e) {
      throw e;
    } catch (Exception e) {
      throw new ModuleException("Failed to parse text document", MuleVectorsErrorType.TRANSFORM_DOCUMENT_PARSING_FAILURE, e);
    }
  }
}
