package org.mule.extension.vectors.internal.service.transform;

import  org.mule.extension.vectors.internal.service.transform.Document;

import java.io.InputStream;

/**
 * Placeholder for a document parser, similar to dev.langchain4j.data.document.DocumentParser.
 */
public interface DocumentParser {
  Document parse(InputStream inputStream);
}
