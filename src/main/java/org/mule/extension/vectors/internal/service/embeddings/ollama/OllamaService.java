package org.mule.extension.vectors.internal.service.embeddings.ollama;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.provider.embeddings.ollama.OllamaModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OllamaService implements EmbeddingService {

    private OllamaModelConnection ollamaModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String EMBEDDINGS_ENDPOINT = "/api/embeddings";

    public OllamaService(OllamaModelConnection ollamaModelConnection, EmbeddingModelParameters embeddingModelParameters) {
        this.ollamaModelConnection = ollamaModelConnection;
        this.embeddingModelParameters = embeddingModelParameters;
    }

    public Object generateTextEmbeddings(List<String> inputs, String modelName) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("Input list cannot be null or empty");
        }
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("Ollama embeddings API accepts only one input at a time");
        }
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be null or empty");
        }

        try {
            return generateTextEmbeddingsAsync(inputs.get(0), modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            if (e.getCause() instanceof ModuleException) {
                throw (ModuleException) e.getCause();
            }
            throw new ModuleException("Failed to generate embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private CompletableFuture<String> generateTextEmbeddingsAsync(String input, String modelName) {
        try {
            byte[] body = buildEmbeddingsPayload(input, modelName);
            return HttpRequestHelper.executePostRequest(this.ollamaModelConnection.getHttpClient(), this.ollamaModelConnection.getBaseUrl() + EMBEDDINGS_ENDPOINT, buildJsonHeaders(), body, (int) this.ollamaModelConnection.getTimeout())
                    .thenApply(this::handleEmbeddingResponse);
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(new ModuleException("Failed to create request body", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
        }
    }

    private String handleEmbeddingResponse(HttpResponse response) {
        return HttpRequestHelper.handleEmbeddingResponse(response, "Ollama Error");
    }

    private byte[] buildEmbeddingsPayload(String input, String modelName) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("prompt", input); // Ollama takes one input at a time
        return objectMapper.writeValueAsBytes(requestBody);
    }

    private Map<String, String> buildJsonHeaders() {
        return Map.of("Content-Type", "application/json");
    }


    @Override
    public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream()
                .map(TextSegment::text)
                .toList();
        {
            List<Embedding> embeddings = new ArrayList<>();

            // Process texts one at a time since Ollama only supports single text embedding
            for (int i = 0; i < texts.size(); i++) {
                try {
                    String response = (String) generateTextEmbeddings(Collections.singletonList(texts.get(i)), embeddingModelParameters.getEmbeddingModelName());
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
                    throw new ModuleException("Failed to generate embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
                }
            }

            return Response.from(embeddings);
        }
    }
}



