package org.mule.extension.vectors.internal.store.aisearch;

import org.mule.extension.vectors.internal.service.VectoreStoreIterator;
import org.mule.extension.vectors.internal.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AISearchStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AISearchStoreIterator.class);
  private final String storeName;
  private final QueryParameters queryParams;
  private final AISearchStoreConnection aiSearchStoreConnection;
  private final String API_VERSION = "2024-07-01";
  private final int dimension;

  private String nextUrl;
  private Iterator<JSONObject> currentBatch;
  private boolean hasMore;
  // Constructor: pass all required fields
  public AISearchStoreIterator(
      String storeName,
      QueryParameters queryParams,
      AISearchStoreConnection aiSearchStoreConnection,
      int dimension
  ) {
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.aiSearchStoreConnection = aiSearchStoreConnection;
    this.nextUrl = buildInitialUrl();
    this.hasMore = true;
    this.dimension = dimension;
    try {
      fetchNextBatch();
    } catch (Exception e) {
      throw new ModuleException("Authentication failed: " , MuleVectorsErrorType.AUTHENTICATION, e);
    }
  }

  private String buildInitialUrl() {
    String ID_DEFAULT_FIELD_NAME = "id";
    String TEXT_DEFAULT_FIELD_NAME = "content";
    String METADATA_DEFAULT_FIELD_NAME = "metadata";
    String VECTOR_DEFAULT_FIELD_NAME = "content_vector";

    String fields = queryParams.retrieveEmbeddings() ?
        String.join(",", ID_DEFAULT_FIELD_NAME, METADATA_DEFAULT_FIELD_NAME,
                    TEXT_DEFAULT_FIELD_NAME, VECTOR_DEFAULT_FIELD_NAME) :
        String.join(",", ID_DEFAULT_FIELD_NAME, METADATA_DEFAULT_FIELD_NAME,
                    TEXT_DEFAULT_FIELD_NAME);

    return aiSearchStoreConnection.getUrl() + "/indexes/" + storeName + "/docs?search=*&$top="
        + queryParams.pageSize() + "&$select=" + fields + "&api-version=" + API_VERSION;
  }

  private void fetchNextBatch() throws Exception {
    if (nextUrl == null) {
      hasMore = false;
      return;
    }

    try {
      HttpClient httpClient = aiSearchStoreConnection.getHttpClient();
      Map<String, String> headers = Map.of("api-key", aiSearchStoreConnection.getApiKey());

      CompletableFuture<HttpResponse> futureResponse =
          HttpRequestHelper.executeGetRequest(httpClient, nextUrl, headers, 5000);

      HttpResponse response = futureResponse.get();

      int responseCode = response.getStatusCode();
      if (responseCode != 200) {
        handleHttpError(response, responseCode);
      }

      String responseBody = readInputStreamToString(response);
      JSONObject jsonResponse = new JSONObject(responseBody);

      JSONArray documents = jsonResponse.getJSONArray("value");
      nextUrl = jsonResponse.optString("@odata.nextLink", null);

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
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    try {
      JSONObject document = currentBatch.next();
      String ID_DEFAULT_FIELD_NAME = "id";
      String TEXT_DEFAULT_FIELD_NAME = "content";
      String METADATA_DEFAULT_FIELD_NAME = "metadata";
      String VECTOR_DEFAULT_FIELD_NAME = "content_vector";

      String embeddingId = document.optString(ID_DEFAULT_FIELD_NAME, null);
      String text = document.optString(TEXT_DEFAULT_FIELD_NAME, null);

      float[] vector = null;

      if (queryParams.retrieveEmbeddings()) {
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

      // This is the only place you may want to adapt for Embedded type.
      // If you want to keep it generic, you can cast or use a factory.
      // For now, we keep it as TextSegment to match the original.
      @SuppressWarnings("unchecked")
      Embedded embedded = (Embedded) new TextSegment(text, metadata);

      return new VectorStoreRow<>(embeddingId,
                                  vector != null ? new Embedding(vector) : null,
                                  embedded);

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
