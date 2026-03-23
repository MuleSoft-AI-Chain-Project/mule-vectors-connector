package org.mule.extension.vectors.api.helper.document;

import java.io.InputStream;

public class TextDocumentParser implements DocumentParser {


  @Override
  public InputStream parse(InputStream inputStream) {
    return inputStream;
  }
}
