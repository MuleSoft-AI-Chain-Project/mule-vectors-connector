package org.mule.extension.vectors.internal.service.embeddings.azureaivision;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.provider.embeddings.azureaivision.AzureAIVisionModelConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.embeddings.EmbeddingService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureAIVisionService implements EmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAIVisionService.class);
    private AzureAIVisionModelConnection azureAIVisionModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;

    public AzureAIVisionService(AzureAIVisionModelConnection azureAIVisionModelConnection, EmbeddingModelParameters embeddingModelParameters) {
        this.azureAIVisionModelConnection = azureAIVisionModelConnection;
        this.embeddingModelParameters = embeddingModelParameters;
    }

    public Object generateTextEmbeddings(List<String> inputs, String modelName) {
        LOGGER.debug("Embedding texts, Model name: {}", modelName);
        if (inputs.size() != 1) {
            throw new UnsupportedOperationException("Azure AI Vision only supports embedding one text at a time");
        }
        try {
            return generateTextEmbeddingsAsync(inputs.get(0), modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Failed to embed text", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private CompletableFuture<String> generateTextEmbeddingsAsync(String text, String modelName) {
        String url = buildUrlWithParams(this.azureAIVisionModelConnection.getEndpoint() + "/computervision/retrieval:vectorizeText",
                Map.of("api-version", this.azureAIVisionModelConnection.getApiVersion(), "model-version", modelName));
        byte[] payload = new JSONObject().put("text", text).toString().getBytes(StandardCharsets.UTF_8);

        return HttpRequestHelper.executePostRequest(this.azureAIVisionModelConnection.getHttpClient(), url, buildHeaders("application/json"), payload, (int) this.azureAIVisionModelConnection.getTimeout())
                .thenApply(this::handleEmbeddingResponse);
    }

    public Object generateImageEmbeddings(List<byte[]> imageBytesList, String modelName) {
        LOGGER.debug("Embedding images, Model name: {}", modelName);
        if (imageBytesList.size() != 1) {
            throw new UnsupportedOperationException("Azure AI Vision only supports embedding one image at a time");
        }
        try {
            return generateImageEmbeddingsAsync(imageBytesList.get(0), modelName).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Failed to embed image", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }
    
    private CompletableFuture<String> generateImageEmbeddingsAsync(byte[] imageBytes, String modelName) {
        String url = buildUrlWithParams(this.azureAIVisionModelConnection.getEndpoint() + "/computervision/retrieval:vectorizeImage",
                Map.of("api-version", this.azureAIVisionModelConnection.getApiVersion(), "model-version", modelName));
        Map<String, String> headers = buildHeaders("application/octet-stream");

        return HttpRequestHelper.executePostRequest(this.azureAIVisionModelConnection.getHttpClient(), url, headers, imageBytes, (int) this.azureAIVisionModelConnection.getTimeout())
                .thenApply(this::handleEmbeddingResponse);
    }

    private String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl);
        if (params != null && !params.isEmpty()) {
            url.append("?");
            params.forEach((key, value) -> {
                try {
                    url.append(URLEncoder.encode(key, StandardCharsets.UTF_8.name()))
                       .append("=")
                       .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()))
                       .append("&");
                } catch (UnsupportedEncodingException e) {
                    // This should not happen with UTF-8
                    throw new ModuleException("Failed to encode URL parameters", MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE, e);
                }
            });
            url.setLength(url.length() - 1); // Remove last '&'
        }
        return url.toString();
    }
    
    private Map<String, String> buildHeaders(String contentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("Ocp-Apim-Subscription-Key", this.azureAIVisionModelConnection.getApiKey());
        return headers;
    }

    private String handleEmbeddingResponse(HttpResponse response) {
        if (response.getStatusCode() != 200) {
            return handleErrorResponse(response, "Failed to generate embedding");
        }
        try {
            return new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ModuleException("Failed to read embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    private String handleErrorResponse(HttpResponse response, String message) {
        try {
            String errorBody = new String(response.getEntity().getBytes(), StandardCharsets.UTF_8);
            String errorMsg = String.format("%s. Azure AI Vision API error (HTTP %d): %s",
                    message, response.getStatusCode(), errorBody);
            LOGGER.error(errorMsg);
            throw new ModuleException(errorMsg, MuleVectorsErrorType.AI_SERVICES_FAILURE);
        } catch (IOException e) {
            throw new ModuleException("Failed to read error response body", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    @Override
    public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream().map(TextSegment::text).toList();
        List<Embedding> embeddings = new ArrayList<>();
        try {
            for(int index = 0; index < texts.size(); index += 1) {
                String response = (String) generateTextEmbeddings(List.of(texts.get(index)), embeddingModelParameters.getEmbeddingModelName());
                JSONObject jsonResponse = new JSONObject(response);
                JSONArray vectorArray = jsonResponse.getJSONArray("vector");
                float[] vector = new float[vectorArray.length()];
                for (int i = 0; i < vectorArray.length(); i++) {
                    vector[i] = (float) vectorArray.getDouble(i);
                }
                embeddings.add(Embedding.from(vector));
            }
            return Response.from(embeddings);
        } catch (Exception e) {
            throw new ModuleException("Failed to process text embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    @Override
    public Response<Embedding> embedImage(byte[] imageBytes) {
        try {
            String response = (String) generateImageEmbeddings(List.of(imageBytes), embeddingModelParameters.getEmbeddingModelName());
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray vectorArray = jsonResponse.getJSONArray("vector");
            float[] vector = new float[vectorArray.length()];
            for (int i = 0; i < vectorArray.length(); i++) {
                vector[i] = (float) vectorArray.getDouble(i);
            }
            return Response.from(Embedding.from(vector));
        } catch (Exception e) {
            throw new ModuleException("Failed to process image embedding response", MuleVectorsErrorType.AI_SERVICES_FAILURE, e);
        }
    }

    @Override
    public Response<Embedding> embedTextAndImage(String text, byte[] imageBytes) {
        LOGGER.warn("Azure AI Vision {} model doesn't support generating embedding for a combination of image and text. The text will not be sent to the model to generate the embeddings.", embeddingModelParameters.getEmbeddingModelName());
        return embedImage(imageBytes);
    }
}

