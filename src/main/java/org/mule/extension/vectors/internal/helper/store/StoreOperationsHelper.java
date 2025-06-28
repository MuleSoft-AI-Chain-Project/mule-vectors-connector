package org.mule.extension.vectors.internal.helper.store;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.ValidationUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.api.metadata.StoreResponseAttributes;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.CustomMetadata;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProviderFactory;
import org.mule.extension.vectors.internal.util.MetadataUtils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.mule.extension.vectors.internal.helper.ResponseHelper.createStoreResponse;

public class StoreOperationsHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreOperationsHelper.class);

    public record ParsedStoreInput(List<TextSegment> textSegments, List<Embedding> embeddings, int dimension, HashMap<String, Object> ingestionMetadata) {}

    public static ParsedStoreInput parseStoreInput(InputStream content, boolean isAddOperation, CustomMetadata customMetadata) throws ModuleException {
        try {
            String contentString = IOUtils.toString(content, StandardCharsets.UTF_8);
            JSONObject jsonContent = new JSONObject(contentString);
            HashMap<String, Object> ingestionMetadataMap = isAddOperation ? MetadataUtils.getIngestionMetadata() : new HashMap<>();

            // Text Segments parsing
            List<TextSegment> textSegments = new LinkedList<>();
            if (jsonContent.has(Constants.JSON_KEY_TEXT_SEGMENTS)) {
                JSONArray jsonTextSegments = jsonContent.getJSONArray(Constants.JSON_KEY_TEXT_SEGMENTS);
                if (!isAddOperation && jsonTextSegments.length() != 1) {
                    throw new ModuleException(
                            String.format("You must provide one text segment only. Received: %s", jsonTextSegments.length()),
                            MuleVectorsErrorType.INVALID_PARAMETER);
                }

                IntStream.range(0, jsonTextSegments.length())
                        .mapToObj(jsonTextSegments::getJSONObject)
                        .forEach(jsonTextSegment -> {
                            HashMap<String, Object> metadataMap = (HashMap<String, Object>) jsonTextSegment.getJSONObject(Constants.JSON_KEY_METADATA).toMap();
                            if (isAddOperation) {
                                metadataMap.putAll(ingestionMetadataMap);
                                if (customMetadata != null && customMetadata.getMetadataEntries() != null) {
                                    metadataMap.putAll(customMetadata.getMetadataEntries());
                                }
                            }
                            Metadata metadata = Metadata.from(metadataMap);
                            textSegments.add(new TextSegment(jsonTextSegment.getString(Constants.JSON_KEY_TEXT), metadata));
                        });
            }

            // Embeddings parsing
            List<Embedding> embeddings = new LinkedList<>();
            JSONArray jsonEmbeddings = jsonContent.getJSONArray(Constants.JSON_KEY_EMBEDDINGS);
            if (!isAddOperation && jsonEmbeddings.length() != 1) {
                throw new ModuleException(String.format("You must provide one embedding only. Received: %s", jsonEmbeddings.length()),
                        MuleVectorsErrorType.INVALID_PARAMETER);
            }
            IntStream.range(0, jsonEmbeddings.length())
                    .mapToObj(jsonEmbeddings::getJSONArray)
                    .forEach(jsonEmbedding -> {
                        float[] floatArray = new float[jsonEmbedding.length()];
                        for (int i = 0; i < jsonEmbedding.length(); i++) {
                            floatArray[i] = (float) jsonEmbedding.getDouble(i);
                        }
                        embeddings.add(new Embedding(floatArray));
                    });

            // Dimension parsing
            int dimension = jsonContent.getInt(Constants.JSON_KEY_DIMENSION);
            ValidationUtils.ensureGreaterThanZero(dimension, Constants.JSON_KEY_DIMENSION);

            return new ParsedStoreInput(textSegments, embeddings, dimension, ingestionMetadataMap);

        } catch (Exception e) {
            if (e instanceof ModuleException) {
                throw (ModuleException) e;
            }
            throw new ModuleException("Error while parsing Text Segments and Embeddings input.", MuleVectorsErrorType.INVALID_PARAMETER, e);
        }
    }

    public static <T> Result<InputStream, StoreResponseAttributes> executeStoreOperation(
            StoreConfiguration storeConfiguration,
            BaseStoreConnection storeConnection,
            String storeName,
            int dimension,
            boolean createStore,
            QueryParameters queryParams,
            Function<VectorStoreService, T> operation,
            Function<T, JSONObject> responseBuilder,
            HashMap<String, Object> attributes) throws ModuleException {

        try {
            VectorStoreService vectorStoreService = VectorStoreServiceProviderFactory.getInstance(storeConfiguration,
                                                                                                  storeConnection,
                                                                                                  storeName,
                                                                                                  queryParams,
                                                                                                  dimension,
                                                                                                  createStore).getService();

            T operationResult = operation.apply(vectorStoreService);
            JSONObject jsonObject = responseBuilder.apply(operationResult);

            return createStoreResponse(jsonObject.toString(), attributes);

        } catch (ModuleException me) {
            throw me;
        } catch (UnsupportedOperationException e) {
            LOGGER.debug(e.getMessage());
            throw new ModuleException(e.getMessage(), MuleVectorsErrorType.STORE_UNSUPPORTED_OPERATION);
        } catch (Exception e) {
            throw new ModuleException(
                    String.format("Error during operation on store %s", storeName),
                    MuleVectorsErrorType.STORE_OPERATIONS_FAILURE,
                    e);
        }
    }

    public static Map<String, Object> getMetadataMap(File file){
       return new HashMap<String, Object>() {{
            put("path", file.getPath());
            put("fileName", file.getFileName());
            put("mimeType", file.getMimeType());
            put("metadata", file.getMetadata());
        }};
    }
} 
