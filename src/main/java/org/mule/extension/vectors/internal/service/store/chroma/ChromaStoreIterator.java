package org.mule.extension.vectors.internal.service.store.chroma;


import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ChromaStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChromaStoreIterator.class);
  private String COLLECTIONS_API_BASE_URL = "/api/v1/collections/";

  private final ChromaStoreConnection chromaStoreConnection;
  private final QueryParameters queryParams;
  private final String storeName;
  private final String collectionId;

  private final List<String> ids = new ArrayList<>();
  private final List<JSONObject> metadata = new ArrayList<>();
  private final List<String> documents = new ArrayList<>();
  private final List<JSONArray> embeddings = new ArrayList<>();
  private int totalCount = 0;
  private int currentPage = 0;
  private int pageIndex = 0;

  public ChromaStoreIterator(
      ChromaStoreConnection chromaStoreConnection,
      String storeName,
      QueryParameters queryParams
  ) throws ModuleException {
    this.chromaStoreConnection = chromaStoreConnection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    try{
      this.collectionId = getCollectionId();
      this.totalCount = getSegmentCount();
      loadNextPage();
    } catch (IOException e){
      throw new ModuleException("Chroma API request failed: ",  MuleVectorsErrorType.STORE_SERVICES_FAILURE, e );
    }

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

      HttpResponse response = futureResponse.get();

      int responseCode = response.getStatusCode();
      String responseBody = readInputStreamToString(response);

      if (responseCode != 200) {
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

  private int getSegmentCount() throws IOException {
    if (collectionId == null) return 0;
    String jsonResponse = getJsonResponse(COLLECTIONS_API_BASE_URL + collectionId + "/count", null);
    Integer count = Integer.parseInt(jsonResponse.toString());
    LOGGER.debug("Segment count {}", count);
    return count;
  }
  private String getCollectionId() throws IOException {
    String jsonResponse = getJsonResponse(COLLECTIONS_API_BASE_URL + storeName, null);
    JSONObject collection = new JSONObject(jsonResponse);
    return collection.getString("id");
  }
  private void loadNextPage() throws IOException {
    if (ids.size() >= totalCount) {
      return;
    }

    String METADATA_DEFAULT_FIELD_NAME = "metadatas";
    String TEXT_DEFAULT_FIELD_NAME = "documents";
    String VECTOR_DEFAULT_FIELD_NAME = "embeddings";
    String ID_DEFAULT_FIELD_NAME = "ids";

    JSONObject jsonRequest = new JSONObject();
    jsonRequest.put("limit", queryParams.pageSize());
    jsonRequest.put("offset", (long) currentPage * queryParams.pageSize());

    JSONArray jsonInclude = new JSONArray();
    jsonInclude.put(METADATA_DEFAULT_FIELD_NAME);
    jsonInclude.put(TEXT_DEFAULT_FIELD_NAME);
    if (queryParams.retrieveEmbeddings()) {
      jsonInclude.put(VECTOR_DEFAULT_FIELD_NAME);
    }
    jsonRequest.put("include", jsonInclude);

    String jsonResponse = getJsonResponse(COLLECTIONS_API_BASE_URL + collectionId + "/get", jsonRequest.toString());

    JSONObject responseObject = new JSONObject(jsonResponse);

    // Clear old data
    ids.clear();
    metadata.clear();
    documents.clear();
    embeddings.clear();
    pageIndex = 0;

    // Populate with new data
    responseObject.optJSONArray(ID_DEFAULT_FIELD_NAME).forEach(id -> ids.add(id.toString()));
    responseObject.optJSONArray(METADATA_DEFAULT_FIELD_NAME).forEach(meta -> metadata.add((JSONObject) meta));
    responseObject.optJSONArray(TEXT_DEFAULT_FIELD_NAME).forEach(doc -> documents.add(doc.toString()));
    if (queryParams.retrieveEmbeddings()) {
      responseObject.optJSONArray(VECTOR_DEFAULT_FIELD_NAME).forEach(emb -> embeddings.add((JSONArray) emb));
    }
    currentPage++;
  }

  @Override
  public boolean hasNext() {
    return (((currentPage-1) * queryParams.pageSize()) + pageIndex) < totalCount;
  }

  @Override
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    if (pageIndex >= ids.size()) {
      try {
        loadNextPage();
      } catch (IOException e) {
        throw new ModuleException("Failed to load next page from Chroma", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
      }
    }

    String id = ids.get(pageIndex);
    Map<String, Object> metadataMap = metadata.get(pageIndex).toMap();
    String text = documents.get(pageIndex);
    JSONArray vectorArray = queryParams.retrieveEmbeddings() ? embeddings.get(pageIndex) : null;

    Embedding embedding = null;
    if (vectorArray != null) {
      float[] vector = new float[vectorArray.length()];
      for (int i = 0; i < vectorArray.length(); i++) {
        vector[i] = (float) vectorArray.getDouble(i);
      }
      embedding = new Embedding(vector);
    }

    // This is the only place you may want to adapt for Embedded type.
    // If you want to keep it generic, you can cast or use a factory.
    // For now, we keep it as TextSegment to match the original.
    @SuppressWarnings("unchecked")
    Embedded embedded = (Embedded) new TextSegment(text, Metadata.from(metadataMap));

    pageIndex++;

    return new VectorStoreRow<>(id, embedding, embedded);
  }
}
