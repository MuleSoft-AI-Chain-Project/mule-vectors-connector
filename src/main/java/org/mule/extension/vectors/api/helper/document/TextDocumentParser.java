package org.mule.extension.vectors.api.helper.document;

import java.io.InputStream;

public class TextDocumentParser implements DocumentParser {


  @Override
  public InputStream parse(InputStream inputStream) {
    return inputStream;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    return o != null && getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
