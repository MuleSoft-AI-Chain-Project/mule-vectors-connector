package org.mule.extension.vectors.internal.model.azureaivision;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AzureAIVisionService implements EmbeddingService {

  private AzureAIVisionModelConnection azureAIVisionModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;
  private Integer dimensions;
  private static final int BATCH_SIZE = 16;

  public AzureAIVisionService(AzureAIVisionModelConnection azureAIVisionModelConnection, EmbeddingModelParameters embeddingModelParameters, Integer dimensions) {
    this.azureAIVisionModelConnection = azureAIVisionModelConnection;
    this.embeddingModelParameters = embeddingModelParameters;
    this.dimensions = dimensions;
  }

  @Override
  public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
    List<String> texts = textSegments.stream().map(TextSegment::text).collect(Collectors.toList());
    {
      List<Embedding> embeddings = new ArrayList<>();
      int tokenUsage = 0;

      for(int x = 0; x < texts.size(); x += BATCH_SIZE) {
        // Extract current batch
        List<String> batch = texts.subList(x, Math.min(x + BATCH_SIZE, texts.size()));

        // Generate embeddings for current batch
        String response = (String) this.azureAIVisionModelConnection.generateTextEmbeddings(batch, embeddingModelParameters.getEmbeddingModelName());
        JSONObject jsonResponse = new JSONObject(response);

        // Accumulate token usage
        tokenUsage += jsonResponse
            .getJSONObject("usage")
            .getInt("total_tokens");

        // Parse embeddings from response
        JSONArray embeddingsArray = jsonResponse.getJSONArray("data");

        // Process each embedding in the response
        for (int i = 0; i < embeddingsArray.length(); i++) {
          JSONObject embeddingObject = embeddingsArray.getJSONObject(i);
          JSONArray embeddingArray = embeddingObject.getJSONArray("embedding");

          // Convert JSON array to float array
          float[] vector = new float[embeddingArray.length()];
          for (int y = 0; y < embeddingArray.length(); y++) {
            vector[y] = (float) embeddingArray.getDouble(y);
          }

          embeddings.add(Embedding.from(vector));
        }
      }

      return Response.from(embeddings, new TokenUsage(tokenUsage));
    }
  }

  @Override
  public Response<Embedding> embedImage(byte[] imageBytes) {
    return null;
  }

  @Override
  public Response<Embedding> embedTextAndImage(String text, byte[] imageBytes) {
    return null;
  }
}

