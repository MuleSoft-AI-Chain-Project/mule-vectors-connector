package org.mule.extension.vectors.api.parameter;

import org.mule.extension.vectors.api.helper.document.DocumentParser;
import org.mule.extension.vectors.api.helper.document.TextDocumentParser;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import java.util.Objects;

@Alias("textDocumentParserParameters")
@DisplayName("Text document parse")
public class TextDocumentParserParameters implements DocumentParserParameters {

  private final String name = "Text document parse";
  private final DocumentParser documentParser = new TextDocumentParser();

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DocumentParser getDocumentParser() {
    return documentParser;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    TextDocumentParserParameters that = (TextDocumentParserParameters) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
