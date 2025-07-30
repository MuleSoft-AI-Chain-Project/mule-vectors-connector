package org.mule.extension.vectors.internal.helper.document;

import java.io.InputStream;

public class TextDocumentParser implements DocumentParser {


  @Override
  public InputStream parse(InputStream inputStream) {
    return inputStream;
  }
}
