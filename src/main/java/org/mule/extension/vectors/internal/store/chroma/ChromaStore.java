package org.mule.extension.vectors.internal.store.chroma;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * ChromaStore is a specialized implementation of {@link BaseStoreService} designed to interact with
 * the Chroma database for managing vector data and sources.
 */
public class ChromaStore extends BaseStoreService {

    static final String ID_DEFAULT_FIELD_NAME = "ids";
    static final String TEXT_DEFAULT_FIELD_NAME = "documents";
    static final String METADATA_DEFAULT_FIELD_NAME = "metadatas";
    static final String VECTOR_DEFAULT_FIELD_NAME = "embeddings";

    private static final Logger LOGGER = LoggerFactory.getLogger(ChromaStore.class);

    private final ChromaStoreConnection chromaStoreConnection;
    private final QueryParameters queryParams;
    private final String collectionId;

    /**
     * Initializes a new instance of ChromaStore.
     *
     * @param storeConfiguration the configuration object containing necessary settings.
     * @param chromaStoreConnection the connection object for ChromaDB
     * @param storeName          the name of the vector store.
     * @param queryParams        parameters related to query configurations.
     * @param dimension          the dimension of the vectors.
     * @param createStore        flag to create the store if it does not exist.
     */
    public ChromaStore(StoreConfiguration storeConfiguration, ChromaStoreConnection chromaStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
        super(storeConfiguration, chromaStoreConnection, storeName, dimension, createStore);
        this.chromaStoreConnection = chromaStoreConnection;
        this.queryParams = queryParams;
        this.collectionId = getCollectionId();

    }

    @Override
    public EmbeddingStore<TextSegment> buildEmbeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(this.chromaStoreConnection.getUrl())
                .collectionName(this.storeName)
                .build();
    }

    @Override
    public ChromaStoreIterator<?> getFileIterator() {
        return new ChromaStoreIterator<>(
            this.chromaStoreConnection,
            this.storeName,
            this.queryParams
        );
    }

    private String getJsonResponse(String endpoint, String jsonBody) {
        try {
            HttpClient httpClient = chromaStoreConnection.getHttpClient();
            CompletableFuture<HttpResponse> futureResponse;
            if (jsonBody != null) {
                futureResponse = HttpRequestHelper.executePostRequest(httpClient, chromaStoreConnection.getUrl() + endpoint, null, jsonBody.getBytes(), 5000);
            } else {
                futureResponse = HttpRequestHelper.executeGetRequest(httpClient, chromaStoreConnection.getUrl() + endpoint, null, 5000);
            }

            HttpResponse response = futureResponse.get(); // Block for the result

            int responseCode = response.getStatusCode();
            String responseBody = readInputStreamToString(response);

            if (responseCode != 200) {
                LOGGER.error("Chroma API request failed with status code {}: {}", responseCode, responseBody);
                throw new ModuleException("Chroma API request failed: " + responseBody, MuleVectorsErrorType.STORE_SERVICES_FAILURE);
            }
            return responseBody;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new ModuleException("Request to Chroma failed or was interrupted", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
        } catch (IOException e) {
            throw new ModuleException("Error reading response from Chroma", MuleVectorsErrorType.NETWORK_ERROR, e);
        }
    }

    private String readInputStreamToString(HttpResponse response) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }
    }

    private String getCollectionId()   {
        try{
            String jsonResponse = getJsonResponse("/api/v1/collections/" + storeName, null);
            JSONObject collection = new JSONObject(jsonResponse);
            return collection.getString("id");
        } catch (Exception e){
            if(!this.createStore)
            throw new ModuleException("Failed to get collection ID from Chroma", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
        }
        return "";

    }

}
