package org.mule.extension.vectors.internal.config;

import static org.mule.runtime.api.meta.ExternalLibraryType.DEPENDENCY;

import org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision.AzureAIVisionModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureopenai.AzureOpenAIModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.embeddings.einstein.EinsteinModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.embeddings.huggingface.HuggingFaceModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.embeddings.mistralai.MistralAIModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.embeddings.nomic.NomicModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.embeddings.ollama.OllamaModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.embeddings.openai.OpenAIModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.embeddings.vertexai.VertexAIModelConnectionProvider;
import org.mule.extension.vectors.internal.operation.EmbeddingOperations;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

@org.mule.runtime.extension.api.annotation.Configuration(name = "embeddingConfig")
@ConnectionProviders({
    AzureOpenAIModelConnectionProvider.class,
    AzureAIVisionModelConnectionProvider.class,
    EinsteinModelConnectionProvider.class,
    HuggingFaceModelConnectionProvider.class,
    MistralAIModelConnectionProvider.class,
    NomicModelConnectionProvider.class,
    OllamaModelConnectionProvider.class,
    VertexAIModelConnectionProvider.class,
    OpenAIModelConnectionProvider.class})
@Operations({EmbeddingOperations.class})
@ExternalLib(name = "LangChain4J",
    type = DEPENDENCY,
    description = "LangChain4J",
    nameRegexpMatcher = "(.*)\\.jar",
    requiredClassName = "dev.langchain4j.data.document.DocumentSplitter",
    coordinates = "dev.langchain4j:langchain4j:1.1.0")
public class EmbeddingConfiguration {


}
