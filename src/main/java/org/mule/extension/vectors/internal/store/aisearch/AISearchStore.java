package org.mule.extension.vectors.internal.store.aisearch;

import com.azure.core.exception.HttpResponseException;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.filter.Filter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.OperationValidator;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.parameter.RemoveFilterParameters;
import org.mule.extension.vectors.internal.helper.parameter.SearchFilterParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.api.util.IOUtils;

import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.joining;

public class AISearchStore extends BaseStoreService {

  static final String ID_DEFAULT_FIELD_NAME = "id";
  static final String TEXT_DEFAULT_FIELD_NAME = "content";
  static final String METADATA_DEFAULT_FIELD_NAME = "metadata";
  static final String VECTOR_DEFAULT_FIELD_NAME = "content_vector";

  private static final String API_VERSION = "2024-07-01";
  private static final Logger LOGGER = LoggerFactory.getLogger(AISearchStore.class);

  private final String apiKey;
  private final String url;
  private final QueryParameters queryParams;
  private final AISearchStoreConnection aiSearchStoreConnection;


  public AISearchStore(StoreConfiguration compositeConfiguration, AISearchStoreConnection aiSearchStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    super(compositeConfiguration, aiSearchStoreConnection, storeName, dimension, createStore);
    this.url = aiSearchStoreConnection.getUrl();
    this.apiKey = aiSearchStoreConnection.getApiKey();
    this.queryParams = queryParams;
    this.aiSearchStoreConnection = aiSearchStoreConnection;
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {
    try {
      return AzureAiSearchEmbeddingStore.builder()
              .endpoint(url)
              .apiKey(apiKey)
              .indexName(storeName)
              .dimensions(dimension > 0 ? dimension : (createStore ? 1536 : 0)) // Default dimension
              .createOrUpdateIndex(createStore)
              .filterMapper(new VectorsAzureAiSearchFilterMapper())
              .build();
    } catch (HttpResponseException e) {
        switch (e.getResponse().getStatusCode()) {
          case 401:
          case 403:
            throw new ModuleException("Authentication failed: " + e.getMessage(), MuleVectorsErrorType.AUTHENTICATION, e);
          case 400:
            throw new ModuleException("Invalid request to Azure AI Search: " + e.getMessage(), MuleVectorsErrorType.INVALID_REQUEST, e);
          default:
            throw new ModuleException("Azure AI Search service error: " + e.getMessage(), MuleVectorsErrorType.SERVICE_ERROR, e);
        }
    } catch (Exception e) {
      throw new ModuleException("Failed to build Azure AI Search embedding store: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  @Override
  public Iterator<BaseStoreService.Row<?>> getRowIterator() {
    try {
      return new AISearchStore.RowIterator();
    } catch (ModuleException e) {
      throw e; // Re-throw ModuleExceptions directly
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator for Azure AI Search", e);
      throw new ModuleException("Failed to create Azure AI Search iterator: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  public class RowIterator implements Iterator<BaseStoreService.Row<?>> {

    private BufferedReader reader;
    private String nextUrl;
    private Iterator<JSONObject> currentBatch;
    private boolean hasMore;

    public RowIterator() throws Exception {
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

      try {
        HttpClient httpClient = aiSearchStoreConnection.getHttpClient();
        Map<String, String> headers = Map.of("api-key", apiKey);

        // The helper is now async
        CompletableFuture<HttpResponse> futureResponse =
            HttpRequestHelper.executeGetRequest(httpClient, nextUrl, headers, 5000);

        // Block and wait for the result
        HttpResponse response = futureResponse.get();

        int responseCode = response.getStatusCode();
        if (responseCode != 200) {
          handleHttpError(response, responseCode);
        }

        String responseBody = readInputStreamToString(response);
        JSONObject jsonResponse = new JSONObject(responseBody);

        JSONArray documents = jsonResponse.getJSONArray("value");
        nextUrl = jsonResponse.optString("@odata.nextLink", null); // Get next page URL

        List<JSONObject> batch = new ArrayList<>();
        for (int i = 0; i < documents.length(); i++) {
          batch.add(documents.getJSONObject(i));
        }
        currentBatch = batch.iterator();
      } catch (ConnectException e) {
        throw new ModuleException("Connection failed: " + e.getMessage(), MuleVectorsErrorType.CONNECTION_FAILED, e);
      } catch (InterruptedException | ExecutionException e) {
        Thread.currentThread().interrupt();
        throw new ModuleException("Request to Azure AI Search failed or was interrupted: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
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

    private void handleHttpError(HttpResponse connection, int responseCode) throws IOException {
        String errorMessage = "Failed to fetch data: " + responseCode + " - " + readInputStreamToString(connection);
        LOGGER.error(errorMessage);

        switch (responseCode) {
            case 401:
            case 403:
                throw new ModuleException("Authentication failed: " + errorMessage, MuleVectorsErrorType.AUTHENTICATION);
            case 400:
                throw new ModuleException("Invalid request to Azure AI Search: " + errorMessage, MuleVectorsErrorType.INVALID_REQUEST);
            default:
                throw new ModuleException("Azure AI Search service error: " + errorMessage, MuleVectorsErrorType.SERVICE_ERROR);
        }
    }

    @Override
    public boolean hasNext() {
      try {
        if (currentBatch == null || (!currentBatch.hasNext() && hasMore)) {
          fetchNextBatch();
        }
      } catch (Exception e) {
        throw new RuntimeException("Error fetching next batch from Azure AI Search", e);
      }
      return currentBatch != null && currentBatch.hasNext();
    }

    @Override
    public BaseStoreService.Row<?> next() {
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
          JSONArray attributes = metadataJsonAttributes.optJSONArray("attributes");
          for (int j = 0; j < attributes.length(); j++) {
            JSONObject attribute = attributes.getJSONObject(j);
            metadataJson.put(attribute.getString("key"), attribute.get("value"));
          }

        } else {
          LOGGER.warn("No metadata available");
        }
        Metadata metadata = metadataJson != null ? Metadata.from(metadataJson.toMap()) : Metadata.from(new HashMap<>());

        return new BaseStoreService.Row<>(embeddingId,
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
