package org.mule.extension.vectors.internal.service.transform;


import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Placeholder for a text document parser that implements our own DocumentParser.
 */
public class TextDocumentParser implements DocumentParser {

  @Override
  public Document parse(InputStream inputStream) {
    try {
      String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      if (text.isBlank()) {
        throw new ModuleException("Empty String", MuleVectorsErrorType.TRANSFORM_OPERATIONS_FAILURE);
      } else {
        return Document.from(text);
      }
    } catch (Exception var4) {
      Exception e = var4;
      throw new RuntimeException(e);
    }
  }
} 
