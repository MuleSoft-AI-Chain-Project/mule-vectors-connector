package org.mule.extension.vectors.internal.model.openai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenAIService implements EmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIService.class);
    private OpenAIModelConnection openAIModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;
    private static final int BATCH_SIZE = 16;

    public OpenAIService(OpenAIModelConnection openAIModelConnection, EmbeddingModelParameters embeddingModelParameters, Integer dimensions) {
        this.openAIModelConnection = openAIModelConnection;
        this.embeddingModelParameters = embeddingModelParameters;
        this.dimensions = dimensions;
    }

    @Override
    public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream().map(TextSegment::text).collect(Collectors.toList());
        {
            List<Embedding> embeddings = new ArrayList<>();
            int tokenUsage = 0;

            for (int x = 0; x < texts.size(); x += BATCH_SIZE) {
                List<String> batch = texts.subList(x, Math.min(x + BATCH_SIZE, texts.size()));
                try {
                    String responseText = (String) openAIModelConnection.generateTextEmbeddings(batch, embeddingModelParameters.getEmbeddingModelName());
                    JSONObject jsonResponse = new JSONObject(responseText);

                    tokenUsage += jsonResponse.getJSONObject("usage").getInt("total_tokens");

                    JSONArray embeddingsArray = jsonResponse.getJSONArray("data");
                    for (int i = 0; i < embeddingsArray.length(); i++) {
                        JSONObject embeddingObject = embeddingsArray.getJSONObject(i);
                        JSONArray embeddingArray = embeddingObject.getJSONArray("embedding");

                        float[] vector = new float[embeddingArray.length()];
                        for (int y = 0; y < embeddingArray.length(); y++) {
                            vector[y] = (float) embeddingArray.getDouble(y);
                        }

                        embeddings.add(Embedding.from(vector));
                    }
                } catch (Exception e) {
                    LOGGER.error("Error generating embeddings", e);
                    throw new RuntimeException("Failed to generate embeddings", e);
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
