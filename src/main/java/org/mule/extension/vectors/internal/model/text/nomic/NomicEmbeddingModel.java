package org.mule.extension.vectors.internal.model.text.nomic;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NomicEmbeddingModel implements EmbeddingModel {

    private final NomicModelConnection connection;
    private final String modelName;

    private NomicEmbeddingModel(NomicModelConnection connection, String modelName) {
        this.connection = connection;
        this.modelName = modelName;
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream()
            .map(TextSegment::text)
            .collect(Collectors.toList());
            
        String responseJson = (String)connection.generateTextEmbeddings(texts, this.modelName);
        JSONObject response = new JSONObject(responseJson);
        JSONArray embeddings = response.getJSONArray("embeddings");
        JSONObject usage = response.getJSONObject("usage");

        List<Embedding> embeddingsList = new ArrayList<>();
        for (int j = 0; j < embeddings.length(); j++) {
            JSONArray embeddingArray = embeddings.getJSONArray(j);
            float[] embeddingValues = new float[embeddingArray.length()];
            for (int i = 0; i < embeddingArray.length(); i++) {
                embeddingValues[i] = (float)embeddingArray.getDouble(i);
            }
            embeddingsList.add(Embedding.from(embeddingValues));
        }

        TokenUsage tokenUsage = new TokenUsage(usage.getInt("total_tokens"), 0);
        return Response.from(embeddingsList, tokenUsage);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private NomicModelConnection connection;
        private String modelName;

        public Builder connection(NomicModelConnection connection) {
            this.connection = connection;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public NomicEmbeddingModel build() {
            return new NomicEmbeddingModel(connection, modelName);
        }
    }
}