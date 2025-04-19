package org.mule.extension.vectors.internal.model.text.ollama;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.DimensionAwareEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.ollama.OllamaModelConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Ollama's embedding model service.
 * This class handles the generation of text embeddings using the Ollama API.
 * Note: Ollama only supports embedding one text at a time.
 */
public class OllamaEmbeddingModel extends DimensionAwareEmbeddingModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(OllamaEmbeddingModel.class);

    private final String modelName;
    private final OllamaModelConnection connection;

    private OllamaEmbeddingModel(OllamaModelConnection connection, String modelName) {
        this.connection = connection;
        this.modelName = modelName;
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream()
                .map(TextSegment::text)
                .collect(Collectors.toList());
        return embedTexts(texts);
    }

    private Response<List<Embedding>> embedTexts(List<String> texts) {
        List<Embedding> embeddings = new ArrayList<>();

        // Process texts one at a time since Ollama only supports single text embedding
        for (int i = 0; i < texts.size(); i++) {
            try {
                String response = (String) connection.generateEmbeddings(Collections.singletonList(texts.get(i)), modelName);
                JSONObject jsonResponse = new JSONObject(response);

                // Get the embedding array directly from the response
                JSONArray embeddingArray = jsonResponse.getJSONArray("embedding");

                // Convert JSON array to float array
                float[] vector = new float[embeddingArray.length()];
                for (int j = 0; j < embeddingArray.length(); j++) {
                    vector[j] = (float) embeddingArray.getDouble(j);
                }

                embeddings.add(Embedding.from(vector));

            } catch (Exception e) {
                LOGGER.error("Error generating embeddings", e);
                throw new RuntimeException("Failed to generate embeddings", e);
            }
        }

        return Response.from(embeddings);
    }

    public static OllamaEmbeddingModelBuilder builder() {
        return new OllamaEmbeddingModelBuilder();
    }

    public static class OllamaEmbeddingModelBuilder {
        private OllamaModelConnection connection;
        private String modelName;

        OllamaEmbeddingModelBuilder() {
        }

        public OllamaEmbeddingModelBuilder connection(OllamaModelConnection connection) {
            this.connection = connection;
            return this;
        }

        public OllamaEmbeddingModelBuilder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public OllamaEmbeddingModel build() {
            return new OllamaEmbeddingModel(connection, modelName);
        }
    }
}