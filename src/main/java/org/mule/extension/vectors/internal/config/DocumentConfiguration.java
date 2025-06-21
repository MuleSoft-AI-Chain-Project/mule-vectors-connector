package org.mule.extension.vectors.internal.config;

import org.mule.extension.vectors.internal.operation.DocumentOperations;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.Operations;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@org.mule.runtime.extension.api.annotation.Configuration(name = "documentConfig")
@Operations({DocumentOperations.class})
@ExternalLib(name = "LangChain4J",
    type=DEPENDENCY,
    description = "LangChain4J",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.data.document.parser.TextDocumentParser",
    coordinates = "dev.langchain4j:langchain4j:1.0.1")
@ExternalLib(name = "LangChain4J Document Parser Apache Tika",
    type=DEPENDENCY,
    description = "LangChain4J Document Parser Apache Tika",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser",
    coordinates = "dev.langchain4j:langchain4j-document-parser-apache-tika:1.0.1-beta6")
public class DocumentConfiguration {

}
