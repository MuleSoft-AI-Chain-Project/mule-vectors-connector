package org.mule.extension.vectors.internal.model.vertexai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.vertexai.VertexAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VertexAIService implements EmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertexAIService.class);
    private VertexAIModelConnection vertexAIModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;
    private static final int BATCH_SIZE = 16;

    public VertexAIService(VertexAIModelConnection vertexAIModelConnection, EmbeddingModelParameters embeddingModelParameters, Integer dimensions) {
        this.vertexAIModelConnection = vertexAIModelConnection;
        this.embeddingModelParameters = embeddingModelParameters;
        this.dimensions = dimensions;
    }

    @Override
    public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
         List<String> texts = textSegments.stream()
          .map(TextSegment::text)
          .collect(Collectors.toList());

      List<Embedding> allEmbeddings = new ArrayList<>();
      for (int i = 0; i < texts.size(); i += vertexAIModelConnection.getBatchSize()) {
        List<String> batch = texts.subList(i, Math.min(i + vertexAIModelConnection.getBatchSize(), texts.size()));
        String result = (String) vertexAIModelConnection.generateTextEmbeddings(batch, embeddingModelParameters.getEmbeddingModelName());
        try {
            JSONObject response = new JSONObject(result);
            JSONArray predictions = response.getJSONArray("predictions");
            List<Embedding> embeddings = new ArrayList<>();
            
            for (int p = 0; p < predictions.length(); p++) {
                JSONObject prediction = predictions.getJSONObject(p);
                List<Float> vector = new ArrayList<>();
                
                JSONObject embeddingsObj = prediction.getJSONObject("embeddings");
                JSONArray values = embeddingsObj.getJSONArray("values");
                
                for (int j = 0; j < values.length(); j++) {
                    vector.add((float) values.getDouble(j));
                }
                embeddings.add(Embedding.from(vector));
            }
            allEmbeddings.addAll(embeddings);
        } catch (Exception e) {
            LOGGER.error("Error generating embeddings", e);
            throw new RuntimeException("Failed to generate embeddings", e);
        }
      }
      return Response.from(allEmbeddings);
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

