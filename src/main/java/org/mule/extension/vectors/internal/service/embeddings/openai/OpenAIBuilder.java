package org.mule.extension.vectors.internal.service.embeddings.openai;

import org.mule.extension.vectors.internal.connection.embeddings.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingServiceBuilder;

/**
 * OpenAIBuilder constructs OpenAIService for embedding operations.
 *
 * <p>Preferred usage: Use via EmbeddingServiceFactoryBuilder.getBuilder(...) as per MDC rule
 * 'Direct Construction of EmbeddingServiceBuilder via EmbeddingServiceFactoryBuilder'.
 * This bypasses the provider indirection for clarity and maintainability.</p>
 */
public class OpenAIBuilder implements EmbeddingServiceBuilder {
    private OpenAIModelConnection openAIModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    public OpenAIBuilder modelParameters(EmbeddingModelParameters embeddingModelParameters) {
      this.embeddingModelParameters = embeddingModelParameters;
      return this;
    }
    public OpenAIBuilder modelConnections(OpenAIModelConnection openAIModelConnection) {
      this.openAIModelConnection = openAIModelConnection;
      return this;
    }

    @Override
    public EmbeddingService build() {
      return new OpenAIService(this.openAIModelConnection, this.embeddingModelParameters);
    }

}



