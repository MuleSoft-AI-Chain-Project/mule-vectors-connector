package org.mule.extension.vectors.internal.model.ollama;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OllamaService implements EmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OllamaService.class);
    private OllamaModelConnection ollamaModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;
    private static final int BATCH_SIZE = 16;

    public OllamaService(OllamaModelConnection ollamaModelConnection, EmbeddingModelParameters embeddingModelParameters, Integer dimensions) {
        this.ollamaModelConnection = ollamaModelConnection;
        this.embeddingModelParameters = embeddingModelParameters;
        this.dimensions = dimensions;
    }

    @Override
    public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream()
                .map(TextSegment::text)
                .collect(Collectors.toList());
        {
            List<Embedding> embeddings = new ArrayList<>();

            // Process texts one at a time since Ollama only supports single text embedding
            for (int i = 0; i < texts.size(); i++) {
                try {
                    String response = (String) ollamaModelConnection.generateTextEmbeddings(Collections.singletonList(texts.get(i)), embeddingModelParameters.getEmbeddingModelName());
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



