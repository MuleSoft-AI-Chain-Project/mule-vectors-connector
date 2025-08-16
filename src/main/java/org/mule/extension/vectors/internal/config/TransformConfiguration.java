package org.mule.extension.vectors.internal.config;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

import org.mule.extension.vectors.internal.operation.TransformOperations;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.Operations;

@org.mule.runtime.extension.api.annotation.Configuration(name = "transformConfig")
@Operations({TransformOperations.class})
@ExternalLib(name = "LangChain4J",
    type = DEPENDENCY,
    description = "LangChain4J",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.data.document.parser.TextDocumentParser",
    coordinates = "dev.langchain4j:langchain4j:1.1.0")
@ExternalLib(name = "LangChain4J Document Parser Apache Tika",
    type = DEPENDENCY,
    description = "LangChain4J Document Parser Apache Tika",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser",
    coordinates = "dev.langchain4j:langchain4j-document-parser-apache-tika:1.1.0-beta7")
public class TransformConfiguration {

}
