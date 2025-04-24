package org.mule.extension.vectors.internal.store.aisearch;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class AISearchStore extends BaseStore {

  static final String ID_DEFAULT_FIELD_NAME = "id";
  static final String TEXT_DEFAULT_FIELD_NAME = "content";
  static final String METADATA_DEFAULT_FIELD_NAME = "metadata";
  static final String VECTOR_DEFAULT_FIELD_NAME = "content_vector";

  private static final String API_VERSION = "2024-07-01";

  private final String apiKey;
  private final String url;

  public AISearchStore(StoreConfiguration compositeConfiguration, AISearchStoreConnection aiSearchStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    super(compositeConfiguration, aiSearchStoreConnection, storeName, queryParams, dimension, createStore);

    this.url = aiSearchStoreConnection.getUrl();
    this.apiKey = aiSearchStoreConnection.getApiKey();
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    return AzureAiSearchEmbeddingStore.builder()
        .endpoint(url)
        .apiKey(apiKey)
        .indexName(storeName)
        .dimensions(dimension > 0 ? dimension : (createStore ? dimension : 1536)) // Default dimension in case of no dimension and no need to create store
        .createOrUpdateIndex(createStore)
        .filterMapper(new VectorsAzureAiSearchFilterMapper())
        .build();
  }

  @Override
  public AISearchStore.RowIterator rowIterator() {
    try {
      return new AISearchStore.RowIterator();
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
  }

  public class RowIterator extends BaseStore.RowIterator {

    private BufferedReader reader;
    private String nextUrl;
    private Iterator<JSONObject> currentBatch;
    private boolean hasMore;

    public RowIterator() throws Exception {
      super();
      this.nextUrl = buildInitialUrl();
      this.hasMore = true;
      fetchNextBatch();
    }

    private String buildInitialUrl() {

      String fields = queryParams.retrieveEmbeddings() ?
          String.join(",", ID_DEFAULT_FIELD_NAME, METADATA_DEFAULT_FIELD_NAME,
                      TEXT_DEFAULT_FIELD_NAME, VECTOR_DEFAULT_FIELD_NAME) :
          String.join(",", ID_DEFAULT_FIELD_NAME, METADATA_DEFAULT_FIELD_NAME,
                      TEXT_DEFAULT_FIELD_NAME);

      return AISearchStore.this.url + "/indexes/" + storeName + "/docs?search=*&$top="
          + queryParams.pageSize() + "&$select=" + fields + "&api-version=" + API_VERSION;
    }

    private void fetchNextBatch() throws Exception {
      if (nextUrl == null) {
        hasMore = false;
        return;
      }

      URL url = new URL(nextUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("api-key", apiKey);

      if (connection.getResponseCode() != 200) {
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        StringBuilder errorResponseBuilder = new StringBuilder();
        String errorLine;
        while ((errorLine = errorReader.readLine()) != null) {
          errorResponseBuilder.append(errorLine);
        }
        errorReader.close();
        LOGGER.error("Failed to fetch data: " + connection.getResponseCode() + " - " + errorResponseBuilder.toString());
        throw new IOException("Failed to fetch data: " + connection.getResponseCode() + " - " + errorResponseBuilder.toString());
      }

      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder responseBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        responseBuilder.append(line);
      }
      reader.close();

      JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
      JSONArray documents = jsonResponse.getJSONArray("value");
      nextUrl = jsonResponse.optString("@odata.nextLink", null); // Get next page URL

      List<JSONObject> batch = new ArrayList<>();
      for (int i = 0; i < documents.length(); i++) {
        batch.add(documents.getJSONObject(i));
      }
      currentBatch = batch.iterator();
    }

    @Override
    public boolean hasNext() {
      try {
        if (currentBatch == null || (!currentBatch.hasNext() && hasMore)) {
          fetchNextBatch();
        }
      } catch (Exception e) {
        LOGGER.error("Error fetching next batch", e);
        return false;
      }
      return currentBatch != null && currentBatch.hasNext();
    }

    @Override
    public Row<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      try {
        JSONObject document = currentBatch.next();
        String embeddingId = document.optString(ID_DEFAULT_FIELD_NAME, null);
        String text = document.optString(TEXT_DEFAULT_FIELD_NAME, null);

        float[] vector = null;

        if(queryParams.retrieveEmbeddings()) {
          JSONArray vectorArray = document.optJSONArray(VECTOR_DEFAULT_FIELD_NAME);
          vector = vectorArray != null ? toFloatArray(vectorArray) : null;
        }

        JSONObject metadataJsonAttributes = document.optJSONObject(METADATA_DEFAULT_FIELD_NAME);
        JSONObject metadataJson = new JSONObject();
        if (metadataJsonAttributes != null) {
          // Extract metadata attributes if available
          JSONArray attributes = metadataJsonAttributes.optJSONArray("attributes");
           // Object to store key-value pairs from attributes
          // Iterate over attributes array to populate sourceObject
          for (int j = 0; j < attributes.length(); j++) {
            JSONObject attribute = attributes.getJSONObject(j);
            metadataJson.put(attribute.getString("key"), attribute.get("value"));
          }

        } else {
          LOGGER.warn("No metadata available");
        }
        Metadata metadata = metadataJson != null ? Metadata.from(metadataJson.toMap()) : Metadata.from(new HashMap<>());

        return new Row<>(embeddingId,
                         vector != null ? new Embedding(vector) : null,
                         new TextSegment(text, metadata));

      } catch (Exception e) {
        LOGGER.error("Error while fetching next row", e);
        throw new NoSuchElementException("Error processing next row");
      }
    }

    private float[] toFloatArray(JSONArray jsonArray) {
      float[] array = new float[jsonArray.length()];
      for (int i = 0; i < jsonArray.length(); i++) {
        array[i] = (float) jsonArray.getDouble(i);
      }
      return array;
    }
  }
}
