package org.mule.extension.vectors.internal.service.embeddings.nomic;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.embeddings.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.entity.multipart.HttpPart;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NomicService implements EmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NomicService.class);
    private NomicModelConnection nomicModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "https://api-atlas.nomic.ai/v1/";
    private static final String TEXT_EMBEDDING_URL = BASE_URL + "embedding/text";
    private static final String IMAGE_EMBEDDING_URL = BASE_URL + "embedding/image";

    public NomicService(NomicModelConnection nomicModelConnection, EmbeddingModelParameters embeddingModelParameters) {
        this.nomicModelConnection = nomicModelConnection;
        this.embeddingModelParameters = embeddingModelParameters;
    }

    public Object generateImageEmbeddings(List<byte[]> imageBytesList, String modelName) {
        if (imageBytesList == null || imageBytesList.isEmpty()) {
            throw new IllegalArgumentException("No images provided for embedding.");
        }
        try {
            return generateImageEmbeddingsAsync(imageBytesList, modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            if (e.getCause() instanceof ModuleException) {
                throw (ModuleException) e.getCause();
            }
            throw new ModuleException("Failed to generate image embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private CompletableFuture<String> generateImageEmbeddingsAsync(List<byte[]> imageBytesList, String modelName) {
        List<HttpPart> parts = buildImageMultipartPayload(imageBytesList, modelName);
        return HttpRequestHelper.executeMultipartPostRequest(this.nomicModelConnection.getHttpClient(), IMAGE_EMBEDDING_URL, buildAuthHeaders(), parts, (int) this.nomicModelConnection.getTimeout())
                .thenApply(this::handleEmbeddingResponse);
    }

    public Object generateTextEmbeddings(List<String> inputs, String modelName) {
        try {
            return generateTextEmbeddingsAsync(inputs, modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            if (e.getCause() instanceof ModuleException) {
                throw (ModuleException) e.getCause();
            }
            throw new ModuleException("Failed to generate text embeddings", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
    
    private CompletableFuture<String> generateTextEmbeddingsAsync(List<String> inputs, String modelName) {
        try {
            byte[] body = buildTextEmbeddingsPayload(inputs, modelName);
            return HttpRequestHelper.executePostRequest(this.nomicModelConnection.getHttpClient(), TEXT_EMBEDDING_URL, buildAuthHeaders(), body, (int) this.nomicModelConnection.getTimeout())
                    .thenApply(this::handleEmbeddingResponse);
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(new ModuleException("Failed to create text embedding request body", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e));
        }
    }

    private String handleEmbeddingResponse(HttpResponse response) {
        if (response.getStatusCode() != 200) {
            return handleErrorResponse(response, "Failed to generate embeddings");
        }
        try {
            return new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ModuleException("Failed to read embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
    
    private List<HttpPart> buildImageMultipartPayload(List<byte[]> imageBytesList, String modelName) {
        List<HttpPart> parts = new ArrayList<>();
        byte[] modelBytes = modelName.getBytes(StandardCharsets.UTF_8);
        parts.add(new HttpPart("model", modelBytes, "text/plain", modelBytes.length));

        int index = 0;
        for (byte[] imageBytes : imageBytesList) {
            String fileName = "image_" + index++ + ".png";
            parts.add(new HttpPart("images", fileName, imageBytes, "image/png", imageBytes.length));
        }
        return parts;
    }

    private byte[] buildTextEmbeddingsPayload(List<String> inputs, String modelName) throws JsonProcessingException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("texts", inputs);
        return objectMapper.writeValueAsBytes(requestBody);
    }

    private Map<String, String> buildAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + this.nomicModelConnection.getApiKey());
        return headers;
    }

    private String handleErrorResponse(HttpResponse response, String message) {
        try {
            String errorBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
            String errorMsg = String.format("%s: HTTP %d. Error: %s", message, response.getStatusCode(), errorBody);
            LOGGER.error(errorMsg);
            throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
        } catch (IOException e) {
            throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    @Override
    public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream()
            .map(TextSegment::text)
            .collect(Collectors.toList());
            
        String responseJson = (String) generateTextEmbeddings(texts, embeddingModelParameters.getEmbeddingModelName());
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

    @Override
    public Response<Embedding> embedImage(byte[] imageBytes) {
        Response<List<Embedding>> response = embedImages(Arrays.asList(imageBytes));
        return Response.from(response.content().get(0), response.tokenUsage());
    }

    @Override
    public Response<Embedding> embedTextAndImage(String text, byte[] imageBytes) {
        LOGGER.warn(String.format("Nomic %s model doesn't support generating embedding for a combination of image and text. " +
            "The text will not be sent to the model to generate the embeddings.", embeddingModelParameters.getEmbeddingModelName()));
        return embedImage(imageBytes);
    }

    public Response<List<Embedding>> embedImages(List<byte[]> imageBytesList) {
        String responseJson = (String) generateImageEmbeddings(imageBytesList, embeddingModelParameters.getEmbeddingModelName());
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
}


