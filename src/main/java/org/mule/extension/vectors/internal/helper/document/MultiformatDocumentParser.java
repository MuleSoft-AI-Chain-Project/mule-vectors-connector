package org.mule.extension.vectors.internal.helper.document;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import org.apache.commons.io.IOUtils;

public class MultiformatDocumentParser implements DocumentParser {

  private ApacheTikaDocumentParser documentParser;

  public MultiformatDocumentParser() {
    this(false);
  }

  public MultiformatDocumentParser(boolean includeMetadata) {
    this.documentParser = includeMetadata ? new ApacheTikaDocumentParser(includeMetadata) : new ApacheTikaDocumentParser();
  }

  @Override
  public InputStream parse(InputStream inputStream) {

    return IOUtils.toInputStream(documentParser.parse(inputStream).text(), StandardCharsets.UTF_8);
  }
}
