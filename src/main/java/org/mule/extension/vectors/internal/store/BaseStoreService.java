package org.mule.extension.vectors.internal.store;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.connection.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.connection.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.connection.store.pgvector.PGVectorStoreConnection;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.connection.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.OperationValidator;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.parameter.RemoveFilterParameters;
import org.mule.extension.vectors.internal.helper.parameter.SearchFilterParameters;
import org.mule.extension.vectors.internal.store.aisearch.AISearchStore;
import org.mule.extension.vectors.internal.store.alloydb.AlloyDBStore;
import org.mule.extension.vectors.internal.store.chroma.ChromaStore;
import org.mule.extension.vectors.internal.store.elasticsearch.ElasticsearchStore;
import org.mule.extension.vectors.internal.store.ephemeralfile.EphemeralFileStore;
import org.mule.extension.vectors.internal.store.milvus.MilvusStore;
import org.mule.extension.vectors.internal.store.mongodbatlas.MongoDBAtlasStore;
import org.mule.extension.vectors.internal.store.opensearch.OpenSearchStore;
import org.mule.extension.vectors.internal.store.pgvector.PGVectorStore;
import org.mule.extension.vectors.internal.store.pinecone.PineconeStore;
import org.mule.extension.vectors.internal.store.qdrant.QdrantStore;
import org.mule.extension.vectors.internal.store.weaviate.WeaviateStore;
import org.mule.extension.vectors.internal.service.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * An abstract base class that provides common implementations for {@link VectorStoreService} methods.
 * It contains shared logic for querying, adding, and removing embeddings, reducing code duplication
 * in concrete store implementations.
 */
public abstract class BaseStoreService implements VectorStoreService {

  protected static final Logger LOGGER = LoggerFactory.getLogger(BaseStoreService.class);

  protected String storeName;
  protected StoreConfiguration storeConfiguration;
  protected BaseStoreConnection storeConnection;
  protected int dimension;
  protected boolean createStore;

  public BaseStoreService(StoreConfiguration storeConfiguration, BaseStoreConnection storeConnection, String storeName, int dimension, boolean createStore) {

    this.storeConfiguration = storeConfiguration;
    this.storeConnection = storeConnection;
    this.storeName = storeName;
    this.dimension = dimension;
    this.createStore = createStore;
  }

  public abstract EmbeddingStore<TextSegment> buildEmbeddingStore();

  @Override
  public JSONObject query(List<TextSegment> textSegments,
                          List<Embedding> embeddings,
                          Number maxResults,
                          Double minScore,
                          SearchFilterParameters searchFilterParams) {

    int maximumResults = maxResults.intValue();

    EmbeddingStore<TextSegment> embeddingStore = buildEmbeddingStore();

    EmbeddingSearchRequest.EmbeddingSearchRequestBuilder searchRequestBuilder = EmbeddingSearchRequest.builder()
            .queryEmbedding(embeddings.get(0))
            .maxResults(maximumResults)
            .minScore(minScore);

    JSONObject jsonObject = new JSONObject();

    if(searchFilterParams != null && searchFilterParams.isConditionSet()) {

      OperationValidator.validateOperationType(
              Constants.STORE_OPERATION_TYPE_FILTER_BY_METADATA, storeConnection.getVectorStore());
      Filter filter = searchFilterParams.buildMetadataFilter();
      searchRequestBuilder.filter(filter);
    }

    EmbeddingSearchRequest searchRequest = searchRequestBuilder.build();

    EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
    List<EmbeddingMatch<TextSegment>> embeddingMatches = searchResult.matches();

    String information = embeddingMatches.stream()
            .map(match -> match.embedded().text())
            .collect(joining("\n\n"));

    jsonObject.put(Constants.JSON_KEY_RESPONSE, information);
    jsonObject.put(Constants.JSON_KEY_STORE_NAME, storeName);
    if(textSegments.size() == 1 && textSegments.get(0).text() != null && !textSegments.get(0).text().isEmpty()) {

      jsonObject.put(Constants.JSON_KEY_QUESTION, textSegments.get(0).text());
    }
    jsonObject.put(Constants.JSON_KEY_MAX_RESULTS, maxResults);
    jsonObject.put(Constants.JSON_KEY_MIN_SCORE, minScore);

    JSONArray sources = new JSONArray();

    JSONObject contentObject;
    for (EmbeddingMatch<TextSegment> match : embeddingMatches) {

      Metadata matchMetadata = match.embedded().metadata();
      contentObject = new JSONObject();
      contentObject.put(Constants.JSON_KEY_EMBEDDING_ID, match.embeddingId());
      contentObject.put(Constants.JSON_KEY_TEXT, match.embedded().text());
      contentObject.put(Constants.JSON_KEY_SCORE, match.score());
      JSONObject metadataObject = new JSONObject(matchMetadata.toMap());
      contentObject.put(Constants.JSON_KEY_METADATA, metadataObject);
      sources.put(contentObject);
    }

    jsonObject.put(Constants.JSON_KEY_SOURCES, sources);

    return jsonObject;
  }

  @Override
  public List<String> add(List<Embedding> embeddings, List<TextSegment> textSegments) {
    EmbeddingStore<TextSegment> embeddingStore = buildEmbeddingStore();
    return embeddingStore.addAll(embeddings, textSegments);
  }

  @Override
  public void remove(RemoveFilterParameters removeFilterParams) {
    EmbeddingStore<TextSegment> embeddingStore = buildEmbeddingStore();
    if (removeFilterParams.isIdsSet()) {
      embeddingStore.removeAll(removeFilterParams.getIds());
    } else if (removeFilterParams.isConditionSet()) {
      Filter filter = removeFilterParams.buildMetadataFilter();
      embeddingStore.removeAll(filter);
    } else {
      embeddingStore.removeAll();
    }
  }

  public abstract Iterator<Row<?>> getRowIterator();


  public static class Row<Embedded> {

    String id;
    Embedding embedding;
    Embedded embedded;

    public Row(String id, Embedding embedding) {
      this(id, embedding, (Embedded)null);
    }

    public Row(String id, Embedding embedding, Embedded embedded) {
      this.id = ValidationUtils.ensureNotBlank(id, "id");
      this.embedding = embedding;
      this.embedded = embedded;
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o != null && this.getClass() == o.getClass()) {
        Row<?> that = (Row)o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.embedding, that.embedding) && Objects.equals(this.embedded, that.embedded);
      } else {
        return false;
      }
    }

    public int hashCode() {
      return Objects.hash(new Object[]{this.id, this.embedding, this.embedded});
    }

    public String getId() {
      return id;
    }

    public Embedding getEmbedding() {
      return embedding;
    }

    public Embedded getEmbedded() {
      return embedded;
    }
  }
}
