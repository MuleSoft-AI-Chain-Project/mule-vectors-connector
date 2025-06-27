package org.mule.extension.vectors.internal.model.nomic;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.model.nomic.NomicModelConnection;
import org.mule.extension.vectors.internal.helper.parameter.EmbeddingModelParameters;
import org.mule.extension.vectors.internal.service.embedding.EmbeddingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NomicService implements EmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NomicService.class);
    private NomicModelConnection nomicModelConnection;
    private EmbeddingModelParameters embeddingModelParameters;
    private Integer dimensions;
    private static final int BATCH_SIZE = 16;

    public NomicService(NomicModelConnection nomicModelConnection, EmbeddingModelParameters embeddingModelParameters, Integer dimensions) {
        this.nomicModelConnection = nomicModelConnection;
        this.embeddingModelParameters = embeddingModelParameters;
        this.dimensions = dimensions;
    }

    @Override
    public Response<List<Embedding>> embedTexts(List<TextSegment> textSegments) {
        List<String> texts = textSegments.stream()
            .map(TextSegment::text)
            .collect(Collectors.toList());
            
        String responseJson = (String)nomicModelConnection.generateTextEmbeddings(texts, embeddingModelParameters.getEmbeddingModelName());
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
        String responseJson = (String)nomicModelConnection.generateImageEmbeddings(imageBytesList, embeddingModelParameters.getEmbeddingModelName());
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


