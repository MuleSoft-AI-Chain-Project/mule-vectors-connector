package org.mule.extension.vectors.internal.model.text.vertexai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import org.mule.extension.vectors.internal.config.EmbeddingConfiguration;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.model.BaseModel;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * Represents a Vertex AI Model that utilizes Google's Vertex AI services to generate embeddings. This model extends the
 * functionality of BaseModel to configure and build an embedding model using Vertex AI tools.
 */
public class VertexAIModel extends BaseModel {

  private static final String DEFAULT_LOCATION = "us-central1";
  private static final String PUBLISHER = "google";

  private VertexAIModelConnection vertexAiModelConnection;

  /**
   * Constructs a new {@link VertexAIModel} instance.
   *
   * @param embeddingConfiguration   The embedding configuration object containing settings for embeddings.
   * @param vertexAiModelConnection  The connection to the Vertex AI Model.
   * @param embeddingModelParameters The parameters required to configure the embedding model.
   */
  public VertexAIModel(EmbeddingConfiguration embeddingConfiguration, VertexAIModelConnection vertexAiModelConnection,
                       EmbeddingModelParameters embeddingModelParameters) {
    super(embeddingConfiguration, vertexAiModelConnection, embeddingModelParameters);
    this.vertexAiModelConnection = vertexAiModelConnection;
  }

  /**
   * Builds and returns an instance of {@link EmbeddingModel} utilizing the Vertex AI services.
   *
   * @return An instance of {@link EmbeddingModel}.
   * @throws ModuleException If there is an error during the initialization of the embedding model.
   */
  public EmbeddingModel buildEmbeddingModel() {
    try {
      return VertexAiEmbeddingModel.builder()
          .predictionServiceClient(vertexAiModelConnection.getPredictionClient())
          .llmUtilityServiceClient(vertexAiModelConnection.getLlmUtilityServiceClient())
          .project(vertexAiModelConnection.getProjectId())
          .location(vertexAiModelConnection.getLocation() != null ? vertexAiModelConnection.getLocation() : DEFAULT_LOCATION)
          .publisher(PUBLISHER)
          .modelName(embeddingModelParameters.getEmbeddingModelName())
          .build();
    } catch (Exception e) {
      throw new ModuleException(
          String.format("Error initializing Vertex AI Embedding Model."),
          MuleVectorsErrorType.AI_SERVICES_FAILURE,
          e);
    }
  }
}
