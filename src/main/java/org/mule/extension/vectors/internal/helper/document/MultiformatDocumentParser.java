package org.mule.extension.vectors.internal.helper.document;

import java.io.InputStream;

import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;

public class MultiformatDocumentParser implements DocumentParser {

  private ApacheTikaDocumentParser documentParser;

  public MultiformatDocumentParser() {
    this(false);
  }

  public MultiformatDocumentParser(boolean includeMetadata) {
    this.documentParser = includeMetadata ? new ApacheTikaDocumentParser(includeMetadata) : new ApacheTikaDocumentParser();
  }

  @Override
  public String parse(InputStream inputStream) {

    return documentParser.parse(inputStream).text();
  }
}
