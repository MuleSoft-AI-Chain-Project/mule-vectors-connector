package org.mule.extension.vectors.api.helper.document;

import java.io.InputStream;

import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import org.apache.commons.io.IOUtils;

public class MultiformatDocumentParser implements DocumentParser {

  private final boolean includeMetadata;
  private ApacheTikaDocumentParser documentParser;

  public MultiformatDocumentParser() {
    this(false);
  }

  public MultiformatDocumentParser(boolean includeMetadata) {
    this.includeMetadata = includeMetadata;
    this.documentParser = includeMetadata ? new ApacheTikaDocumentParser(includeMetadata) : new ApacheTikaDocumentParser();
  }

  @Override
  public InputStream parse(InputStream inputStream) {

    return IOUtils.toInputStream(documentParser.parse(inputStream).text());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MultiformatDocumentParser that = (MultiformatDocumentParser) o;
    return includeMetadata == that.includeMetadata;
  }

  @Override
  public int hashCode() {
    return Boolean.hashCode(includeMetadata);
  }
}
