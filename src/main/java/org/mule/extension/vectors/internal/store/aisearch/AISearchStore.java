package org.mule.extension.vectors.internal.store.aisearch;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;
import org.mule.extension.vectors.internal.util.LangChain4JJsonUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class AISearchStore extends BaseStore {

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
        .dimensions(dimension)
        .createOrUpdateIndex(createStore)
        .build();
  }

  public JSONObject listSources() {

    HashMap<String, JSONObject> sourceObjectMap = new HashMap<String, JSONObject>();

    JSONObject jsonObject = new JSONObject();
    jsonObject.put(Constants.JSON_KEY_STORE_NAME, storeName);

    int segmentCount = 0; // Counter to track the number of segments processed
    int offset = 0; // Initialize offset for pagination

    try {

      boolean hasMore = true; // Flag to check if more pages are available

      // Loop to process pages until no more documents are available
      do {
        // Construct the URL with $top and $skip for pagination
        String urlString = this.url + "/indexes/" + storeName + "/docs?search=*&$top=" + queryParams.embeddingPageSize() +
            "&$skip=" + offset + "&$select=id," + Constants.STORE_SCHEMA_METADATA_FIELD_NAME + "&api-version=" + API_VERSION;

        // Nested loop to handle each page of results
        while (urlString != null) {

          URL url = new URL(urlString);

          // Open connection and configure HTTP request
          HttpURLConnection connection = (HttpURLConnection) url.openConnection();
          connection.setRequestMethod("GET");
          connection.setRequestProperty("Content-Type", "application/json");
          connection.setRequestProperty("api-key", apiKey);

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
            JSONArray documents = jsonResponse.getJSONArray("value");

            // Iterate over each document in the current page
            for (int i = 0; i < documents.length(); i++) {

              JSONObject document = documents.getJSONObject(i);
              String id = document.getString("id"); // Document ID
              JSONObject metadata = document.optJSONObject(Constants.STORE_SCHEMA_METADATA_FIELD_NAME); // Metadata of the document

              if (metadata != null) {

                // Extract metadata attributes if available
                JSONArray attributes = metadata.optJSONArray("attributes");

                JSONObject metadataObject = new JSONObject(); // Object to store key-value pairs from attributes
                // Iterate over attributes array to populate sourceObject
                for (int j = 0; j < attributes.length(); j++) {

                  JSONObject attribute = attributes.getJSONObject(j);
                  metadataObject.put(attribute.getString("key"), attribute.get("value"));
                }

                JSONObject sourceObject = getSourceObject(metadataObject);
                addOrUpdateSourceObjectIntoSourceObjectMap(sourceObjectMap, sourceObject);

                LOGGER.debug("sourceObject: " + sourceObject);
                segmentCount++; // Increment document count
              } else {
                LOGGER.warn("No metadata available");
              }
            }

            // Check for the next page link in the response
            urlString = jsonResponse.optString("@odata.nextLink", null);

            // If there is no next page, check if fewer documents were returned than PAGE_SIZE
            if (urlString == null && documents.length() < queryParams.embeddingPageSize()) {
              hasMore = false; // No more documents to retrieve
            }

          } else {
            // Log any error responses from the server
            LOGGER.error("Error: " + connection.getResponseCode() + " " + connection.getResponseMessage());
            break;
          }
        }

        // Increment offset to fetch the next segment of documents
        offset += queryParams.embeddingPageSize();

      } while (hasMore); // Continue if more pages are available

      // Output total count of processed documents
      LOGGER.debug(Constants.JSON_KEY_SEGMENT_COUNT + ": " + segmentCount);

    } catch (Exception e) {

      // Handle any exceptions that occur during the process
      LOGGER.error("Error while listing sources", e);
    }

    jsonObject.put(Constants.JSON_KEY_SOURCES, LangChain4JJsonUtils.jsonObjectCollectionToJsonArray(sourceObjectMap.values()));
    jsonObject.put(Constants.JSON_KEY_SOURCE_COUNT, sourceObjectMap.size());

    return jsonObject;
  }
}
