package org.mule.extension.vectors.internal.model.text.openai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.openai.OpenAIModelConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class OpenAIEmbeddingModel extends DimensionAwareEmbeddingModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIEmbeddingModel.class);
    private static final int BATCH_SIZE = 16;

    private final String modelName;
    private final Integer dimensions;
    private final OpenAIModelConnection connection;

    private OpenAIEmbeddingModel(OpenAIModelConnection connection, String modelName, Integer dimensions) {
        this.connection = connection;
        this.modelName = modelName;
        this.dimensions = dimensions;
    }

    protected Integer knownDimension() {
        return this.dimensions != null ? this.dimensions : OpenAIEmbeddingModelName.knownDimension(this.modelName());
    }

    public String modelName() {
        return this.modelName;
    }

    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream().map(TextSegment::text).collect(Collectors.toList());
        return this.embedTexts(texts);
    }

    private Response<List<Embedding>> embedTexts(List<String> texts) {
        List<Embedding> embeddings = new ArrayList<>();
        int tokenUsage = 0;

        for (int x = 0; x < texts.size(); x += BATCH_SIZE) {
            List<String> batch = texts.subList(x, Math.min(x + BATCH_SIZE, texts.size()));
            try {
                String responseText = (String) connection.generateTextEmbeddings(batch, modelName);
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OpenAIModelConnection connection;
        private String modelName;
        private Integer dimensions;

        public Builder connection(OpenAIModelConnection connection) {
            this.connection = connection;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder modelName(OpenAIEmbeddingModelName modelName) {
            this.modelName = modelName.toString();
            return this;
        }

        public Builder dimensions(Integer dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public OpenAIEmbeddingModel build() {
            return new OpenAIEmbeddingModel(connection, modelName, dimensions);
        }
    }
}