package org.mule.extension.vectors.internal.store.chroma;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;
import org.mule.extension.vectors.internal.util.JsonUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * ChromaStore is a specialized implementation of {@link BaseStore} designed to interact with
 * the Chroma database for managing vector data and sources.
 */
public class ChromaStore extends BaseStore {

  static final String ID_DEFAULT_FIELD_NAME = "id";
  static final String TEXT_DEFAULT_FIELD_NAME = "text";
  static final String METADATA_DEFAULT_FIELD_NAME = "metadata";
  static final String VECTOR_DEFAULT_FIELD_NAME = "vector";

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

  /**
   * Retrieves a JSON object listing all sources associated with the store.
   *
   * @return a {@link JSONObject} containing details of all sources.
   */
  public JSONObject listSources() {

    HashMap<String, JSONObject> sourceObjectMap = new HashMap<String, JSONObject>();

    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.JSON_KEY_STORE_NAME, storeName);

    long segmentCount = 0; // Counter to track the number of segments processed
    long offset = 0; // Initialize offset for pagination

    try {

      String collectionId = getCollectionId(storeName);
      segmentCount = getSegmentCount(collectionId);

      while(offset < segmentCount) {

        JSONArray metadataObjects = getMetadataObjects(collectionId, offset, queryParams.embeddingPageSize());
        for(int i = 0; i< metadataObjects.length(); i++) {

          JSONObject metadataObject = metadataObjects.getJSONObject(i);
          JSONObject sourceObject = getSourceObject(metadataObject);
          addOrUpdateSourceObjectIntoSourceObjectMap(sourceObjectMap, sourceObject);
        }
        offset = offset + metadataObjects.length();
      }

    } catch (Exception e) {

      // Handle any exceptions that occur during the process
      LOGGER.error("Error while listing sources", e);
    }

    jsonObject.put(Constants.JSON_KEY_SOURCES, JsonUtils.jsonObjectCollectionToJsonArray(sourceObjectMap.values()));
    jsonObject.put(Constants.JSON_KEY_SOURCE_COUNT, sourceObjectMap.size());

    return jsonObject;
  }

  private JSONArray getMetadataObjects(String collectionId, long offset, long limit) {

    JSONArray metadataObjects = new JSONArray();
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
      jsonInclude.put("metadatas");

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
        JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
        metadataObjects = jsonResponse.getJSONArray("metadatas");

      } else {

        // Log any error responses from the server
        LOGGER.error("Error: " + connection.getResponseCode() + " " + connection.getResponseMessage());
      }

    } catch (Exception e) {

      // Handle any exceptions that occur during the process
      LOGGER.error("Error getting collection segments", e);
    }
    return metadataObjects;
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


    public RowIterator() throws Exception {

      super();
    }

    @Override
    public boolean hasNext() {

      return true;
    }

    @Override
    public Row<?> next() {
      try {

        return null;

      } catch (Exception e) {
        LOGGER.error("Error while fetching next row", e);
        return null;
      }
    }
  }
}
