package org.mule.extension.vectors.internal.config;

import org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision.AzureAIVisionModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureopenai.AzureOpenAIModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.embeddings.einstein.EinsteinModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.embeddings.huggingface.HuggingFaceModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.embeddings.mistralai.MistralAIModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.embeddings.nomic.NomicModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.embeddings.ollama.OllamaModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.embeddings.openai.OpenAIModelConnectionProvider;
import org.mule.extension.vectors.internal.operation.EmbeddingOperations;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

@org.mule.runtime.extension.api.annotation.Configuration(name = "embeddingConfig")
@ConnectionProviders({
    AzureOpenAIModelConnectionProvider.class,
    AzureAIVisionModelConnectionProvider.class,
    EinsteinModelConnectionProvider.class,
    HuggingFaceModelConnectionProvider.class,
    MistralAIModelConnectionProvider.class,
    NomicModelConnectionProvider.class,
    OllamaModelConnectionProvider.class,
    OpenAIModelConnectionProvider.class})
@Operations({EmbeddingOperations.class})
@ExternalLib(name = "LangChain4J",
    type=DEPENDENCY,
    description = "LangChain4J",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.data.document.DocumentSplitter",
    coordinates = "dev.langchain4j:langchain4j:1.1.0")
public class EmbeddingConfiguration {


}
