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
import org.mule.extension.vectors.internal.store.BaseStore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * ChromaStore is a specialized implementation of {@link BaseStore} designed to interact with
 * the Chroma database for managing vector data and sources.
 */
public class ChromaStore extends BaseStore {

  static final String ID_DEFAULT_FIELD_NAME = "ids";
  static final String TEXT_DEFAULT_FIELD_NAME = "documents";
  static final String METADATA_DEFAULT_FIELD_NAME = "metadatas";
  static final String VECTOR_DEFAULT_FIELD_NAME = "embeddings";

  private final String url;

  /**
   * Initializes a new instance of ChromaStore.
   *
   * @param storeName     the name of the vector store.
   * @param storeConfiguration the configuration object containing necessary settings.
   * @param queryParams   parameters related to query configurations.
   */
  public ChromaStore(StoreConfiguration storeConfiguration, ChromaStoreConnection chromaStoreConnection, String storeName, QueryParameters queryParams, int dimension) {

    super(storeConfiguration, chromaStoreConnection, storeName, queryParams, dimension, true);

    this.url = chromaStoreConnection.getUrl();
  }

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
      if(queryParams.retrieveEmbeddings()) jsonInclude.put(VECTOR_DEFAULT_FIELD_NAME);

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
  public ChromaStore.RowIterator rowIterator() {
    try {
      return new ChromaStore.RowIterator();
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
  }

  public class RowIterator extends BaseStore.RowIterator {

    private List<String> idsObjects;
    private List<JSONObject> metadataObjects;
    private List<String> documentsObjects;
    private List<JSONArray> embeddingsObjects;
    private int currentIndex;

    public RowIterator() throws Exception {
      super();
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
      long offset = 0;

      while (offset < segmentCount) {
        JSONObject jsonResponse = getJsonResponse(collectionId, offset, queryParams.pageSize());
        JSONArray jsonArrayIds = jsonResponse.getJSONArray(ID_DEFAULT_FIELD_NAME); 
        JSONArray jsonArrayMetadatas = jsonResponse.getJSONArray(METADATA_DEFAULT_FIELD_NAME);
        JSONArray jsonArrayDocuments = jsonResponse.getJSONArray(TEXT_DEFAULT_FIELD_NAME);
        JSONArray jsonArrayEmbeddings = queryParams.retrieveEmbeddings() ? jsonResponse.getJSONArray(VECTOR_DEFAULT_FIELD_NAME) : null;
        for (int i = 0; i < jsonArrayIds.length(); i++) {
          idsObjects.add(jsonArrayIds.getString(i));
          metadataObjects.add(jsonArrayMetadatas.getJSONObject(i));
          documentsObjects.add(jsonArrayDocuments.getString(i));
          if(queryParams.retrieveEmbeddings()) embeddingsObjects.add(jsonArrayEmbeddings.getJSONArray(i));
        }
        offset += jsonArrayIds.length();
      }
    }

    @Override
    public boolean hasNext() {
      return currentIndex < metadataObjects.size();
    }

    @Override
    public Row<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      try {

        String embeddingId = idsObjects.get(currentIndex);
        JSONObject metadataObject = metadataObjects.get(currentIndex);
        String text = documentsObjects.get(currentIndex);

        float[] vector = null;
        if(queryParams.retrieveEmbeddings()) {

          JSONArray vectorArray = embeddingsObjects.get(currentIndex);
          vector = new float[vectorArray.length()];
          for (int j = 0; j < vectorArray.length(); j++) {
            vector[j] = vectorArray.getFloat(j);
          }
        }

        currentIndex++;

        return new Row<>(embeddingId,
                         vector != null ? new Embedding(vector) : null,
                         new TextSegment(text, Metadata.from(metadataObject.toMap())));
      } catch (Exception e) {
        LOGGER.error("Error while fetching next row", e);
        throw new NoSuchElementException("No more elements available");
      }
    }
  }
}
