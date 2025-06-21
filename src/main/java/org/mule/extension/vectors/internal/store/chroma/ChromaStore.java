package org.mule.extension.vectors.internal.store.chroma;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * ChromaStore is a specialized implementation of {@link BaseStoreService} designed to interact with
 * the Chroma database for managing vector data and sources.
 */
public class ChromaStore extends BaseStoreService {

    static final String ID_DEFAULT_FIELD_NAME = "ids";
    static final String TEXT_DEFAULT_FIELD_NAME = "documents";
    static final String METADATA_DEFAULT_FIELD_NAME = "metadatas";
    static final String VECTOR_DEFAULT_FIELD_NAME = "embeddings";

    private final String url;
    private final QueryParameters queryParams;

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

        this.url = chromaStoreConnection.getUrl();
        this.queryParams = queryParams;
    }

    @Override
    public EmbeddingStore<TextSegment> buildEmbeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(this.url)
                .collectionName(storeName)
                .build();
    }

    private JSONObject getJsonResponse(String collectionId, long offset, long limit) {

        JSONObject jsonResponse = new JSONObject();
        try {

            String urlString = url + "/api/v1/collections/" + collectionId + "/get";
            URL url = new URL(urlString);

            // Open connection and configure HTTP request
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true); // Enable output for the connection

            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("limit", limit);
            jsonRequest.put("offset", offset);

            JSONArray jsonInclude = new JSONArray();
            jsonInclude.put(METADATA_DEFAULT_FIELD_NAME);
            jsonInclude.put(TEXT_DEFAULT_FIELD_NAME);
            if (queryParams.retrieveEmbeddings()) jsonInclude.put(VECTOR_DEFAULT_FIELD_NAME);

            jsonRequest.put("include", jsonInclude);

            // Write JSON body to the request output stream
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Check the response code and handle accordingly
            if (connection.getResponseCode() == 200) {

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;

                // Read response line by line
                while ((line = in.readLine()) != null) {
                    responseBuilder.append(line);
                }
                in.close();

                // Parse JSON response
                jsonResponse = new JSONObject(responseBuilder.toString());

            } else {

                // Log any error responses from the server
                LOGGER.error("Error: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

        } catch (Exception e) {

            // Handle any exceptions that occur during the process
            LOGGER.error("Error getting collection segments", e);
        }
        return jsonResponse;
    }

    /**
     * Retrieves the total number of segments in the specified collection.
     *
     * @param collectionId the ID of the collection.
     * @return the segment count as a {@code long}.
     */
    private long getSegmentCount(String collectionId) {

        long segmentCount = 0;
        try {

            String urlString = url + "/api/v1/collections/" + collectionId + "/count";
            URL url = new URL(urlString);

            // Open connection and configure HTTP request
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            // Check the response code and handle accordingly
            if (connection.getResponseCode() == 200) {

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;

                // Read response line by line
                while ((line = in.readLine()) != null) {
                    responseBuilder.append(line);
                }
                in.close();
                segmentCount = Long.parseLong(responseBuilder.toString());

            } else {

                // Log any error responses from the server
                LOGGER.error("Error: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

        } catch (Exception e) {

            // Handle any exceptions that occur during the process
            LOGGER.error("Error getting collection count", e);
        }
        LOGGER.debug("segmentCount: " + segmentCount);
        return segmentCount;
    }

    /**
     * Retrieves the collection ID for a given store name.
     *
     * @param storeName the name of the store.
     * @return the collection ID as a {@code String}.
     */
    private String getCollectionId(String storeName) {

        String collectionId = "";
        try {

            String urlString = url + "/api/v1/collections/" + storeName;
            URL url = new URL(urlString);

            // Open connection and configure HTTP request
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            // Check the response code and handle accordingly
            if (connection.getResponseCode() == 200) {

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;

                // Read response line by line
                while ((line = in.readLine()) != null) {
                    responseBuilder.append(line);
                }
                in.close();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                collectionId = jsonResponse.getString("id");

            } else {

                // Log any error responses from the server
                LOGGER.error("Error: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

        } catch (Exception e) {

            // Handle any exceptions that occur during the process
            LOGGER.error("Error getting collection id", e);
        }
        LOGGER.debug("collectionId: " + collectionId);
        return collectionId;
    }

    @Override
    public Iterator<BaseStoreService.Row<?>> getRowIterator() {
        try {
            return new ChromaStore.RowIterator();
        } catch (Exception e) {
            LOGGER.error("Error while creating row iterator", e);
            throw new RuntimeException(e);
        }
    }

    public class RowIterator implements Iterator<BaseStoreService.Row<?>> {

        private List<String> idsObjects;
        private List<JSONObject> metadataObjects;
        private List<String> documentsObjects;
        private List<JSONArray> embeddingsObjects;
        private int currentIndex;

        public RowIterator() throws Exception {
            this.idsObjects = new ArrayList<>();
            this.metadataObjects = new ArrayList<>();
            this.documentsObjects = new ArrayList<>();
            this.embeddingsObjects = new ArrayList<>();
            this.currentIndex = 0;
            loadObjects();
        }

        private void loadObjects() throws Exception {
            String collectionId = getCollectionId(storeName);
            long segmentCount = getSegmentCount(collectionId);
            long pageSize = queryParams.pageSize();

            for (long offset = 0; offset < segmentCount; offset += pageSize) {
                JSONObject jsonResponse = getJsonResponse(collectionId, offset, pageSize);

                JSONArray ids = jsonResponse.getJSONArray(ID_DEFAULT_FIELD_NAME);
                JSONArray metadatas = jsonResponse.getJSONArray(METADATA_DEFAULT_FIELD_NAME);
                JSONArray documents = jsonResponse.getJSONArray(TEXT_DEFAULT_FIELD_NAME);
                JSONArray embeddings = null;
                if(queryParams.retrieveEmbeddings()) {
                    embeddings = jsonResponse.getJSONArray(VECTOR_DEFAULT_FIELD_NAME);
                }


                for (int i = 0; i < ids.length(); i++) {
                    this.idsObjects.add(ids.getString(i));
                    this.metadataObjects.add(metadatas.getJSONObject(i));
                    this.documentsObjects.add(documents.getString(i));
                    if (embeddings != null) {
                        this.embeddingsObjects.add(embeddings.getJSONArray(i));
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return currentIndex < idsObjects.size();
        }

        @Override
        public BaseStoreService.Row<?> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            try {
                String embeddingId = idsObjects.get(currentIndex);
                JSONObject metadataJson = metadataObjects.get(currentIndex);
                String text = documentsObjects.get(currentIndex);
                JSONArray vectorArray = null;
                if (queryParams.retrieveEmbeddings()) {
                    vectorArray = embeddingsObjects.get(currentIndex);
                }

                float[] vector = null;
                if(vectorArray != null) {
                    vector = new float[vectorArray.length()];
                    for (int i = 0; i < vectorArray.length(); i++) {
                        vector[i] = (float) vectorArray.getDouble(i);
                    }
                }

                currentIndex++;

                return new BaseStoreService.Row<>(embeddingId,
                        vector != null ? new Embedding(vector) : null,
                        new TextSegment(text, Metadata.from(metadataJson.toMap())));

            } catch (Exception e) {
                LOGGER.error("Error while fetching next row", e);
                throw new NoSuchElementException("Error processing next row");
            }
        }
    }
}
