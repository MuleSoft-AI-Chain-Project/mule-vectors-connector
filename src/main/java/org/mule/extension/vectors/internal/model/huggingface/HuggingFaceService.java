package org.mule.extension.vectors.internal.model.huggingface;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.huggingface.HuggingFaceModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HuggingFaceService implements EmbeddingService {

  private HuggingFaceModelConnection huggingFaceModelConnection;
  private EmbeddingModelParameters embeddingModelParameters;
  private Integer dimensions;
  private static final int BATCH_SIZE = 16;

  public HuggingFaceService(HuggingFaceModelConnection huggingFaceModelConnection, EmbeddingModelParameters embeddingModelParameters, Integer dimensions) {
    this.huggingFaceModelConnection = huggingFaceModelConnection;
    this.embeddingModelParameters = embeddingModelParameters;
    this.dimensions = dimensions;
  }

  @Override
  public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
    List<String> texts = textSegments.stream()
          .map(TextSegment::text)
          .collect(Collectors.toList());

    String result = (String) huggingFaceModelConnection.generateTextEmbeddings(texts, embeddingModelParameters.getEmbeddingModelName());
    {
        List<Embedding> embeddings = new ArrayList<>();
        JSONArray embeddingsArray = new JSONArray(result);
        
        for (int i = 0; i < embeddingsArray.length(); i++) {
            JSONArray embeddingArray = embeddingsArray.getJSONArray(i);
            float[] vector = new float[embeddingArray.length()];
            
            for (int j = 0; j < embeddingArray.length(); j++) {
            vector[j] = (float) embeddingArray.getDouble(j);
            }
            
            embeddings.add(Embedding.from(vector));
        }
        return Response.from(embeddings);
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

