package org.mule.extension.vectors.internal.config;

import org.mule.extension.vectors.internal.connection.model.azureaivision.AzureAIVisionModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.model.azureopenai.AzureOpenAIModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.model.einstein.EinsteinModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.model.mistralai.MistralAIModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.model.ollama.OllamaModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnectionProvider;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnectionProvider;
import org.mule.extension.vectors.internal.operation.EmbeddingOperations;
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
    OpenAIModelConnectionProvider.class,
    VertexAIModelConnectionProvider.class})
@Operations({EmbeddingOperations.class})
public class EmbeddingConfiguration {


}
