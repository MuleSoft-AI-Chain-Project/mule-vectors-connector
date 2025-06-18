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
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * The {@code VectorStore} class provides a framework for interacting with various types of vector stores,
 * enabling storage and retrieval of vector embeddings for data analysis and retrieval purposes. It serves as
 * an abstract base for specific implementations such as Milvus, PGVector, and AI Search stores.
 */
public class BaseStore {

  protected static final Logger LOGGER = LoggerFactory.getLogger(BaseStore.class);

  protected String storeName;
  protected StoreConfiguration storeConfiguration;
  protected BaseStoreConnection storeConnection;
  protected QueryParameters queryParams;
  protected int dimension;
  protected boolean createStore;

  public BaseStore(StoreConfiguration storeConfiguration, BaseStoreConnection storeConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    this.storeConfiguration = storeConfiguration;
    this.storeConnection = storeConnection;
    this.storeName = storeName;
    this.queryParams = queryParams;
    this.dimension = dimension;
    this.createStore = createStore;
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

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

  /**
   * Provides a {@link Builder} instance for configuring and creating {@code BaseStore} objects.
   * <p>
   * The builder pattern allows for more flexible and readable configuration of a {@code BaseStore}.
   * Use this to set parameters such as the store name, configuration, query parameters, and embedding model.
   * </p>
   *
   * @return a new {@code Builder} instance.
   */
  public static Builder builder() {

    return new Builder();
  }

  /**
   * Builder class for creating instances of {@link BaseStore}.
   * <p>
   * The {@code Builder} class allows you to set various configuration parameters before
   * creating a {@code BaseStore} instance. These parameters include the store name,
   * configuration settings, query parameters, and embedding model details.
   * </p>
   */
  public static class Builder {

    private StoreConfiguration storeConfiguration;
    private BaseStoreConnection storeConnection;
    private QueryParameters queryParams;
    private String storeName;
    private int dimension;
    private boolean createStore = true;

    public Builder() {

    }

    /**
     * Sets the store name for the {@code BaseStore}.
     *
     * @param storeName the name of the vector store.
     * @return the {@code Builder} instance, for method chaining.
     */
    public Builder storeName(String storeName) {
      this.storeName = storeName;
      return this;
    }

    /**
     * Sets the configuration for the {@code BaseStore}.
     *
     * @param storeConfiguration the configuration parameters.
     * @return the {@code Builder} instance, for method chaining.
     */
    public Builder configuration(StoreConfiguration storeConfiguration) {
      this.storeConfiguration = storeConfiguration;
      return this;
    }

    public Builder connection(BaseStoreConnection storeConnection) {
      this.storeConnection = storeConnection;
      return this;
    }

    /**
     * Sets the query parameters for embedding searches.
     *
     * @param queryParams the query parameters to use.
     * @return the {@code Builder} instance, for method chaining.
     */
    public Builder queryParams(QueryParameters queryParams) {
      this.queryParams = queryParams;
      return this;
    }

    public Builder dimension(int dimension) {
      this.dimension = dimension;
      return this;
    }

    public Builder createStore(boolean createStore) {
      this.createStore = createStore;
      return this;
    }

    /**
     * Builds and returns a new {@link BaseStore} instance based on the builder's configuration.
     * <p>
     * Depending on the specified configuration, it returns an instance of the appropriate
     * store class (e.g., {@link MilvusStore}, {@link PGVectorStore}, or {@link AISearchStore}).
     * If no matching store configuration is found, it returns a default {@code BaseStore} instance.
     * </p>
     *
     * @return a {@code BaseStore} instance.
     * @throws IllegalArgumentException if the configured vector store is unsupported.
     */
    public BaseStore build() {

      BaseStore baseStore;

      LOGGER.debug("Vector Store: " + storeConnection.getVectorStore());
      switch (storeConnection.getVectorStore()) {

        case Constants.VECTOR_STORE_MILVUS:

          baseStore = new MilvusStore(storeConfiguration, (MilvusStoreConnection)storeConnection, storeName, queryParams, dimension);
          break;

        case Constants.VECTOR_STORE_MONGODB_ATLAS:

          baseStore = new MongoDBAtlasStore(storeConfiguration, (MongoDBAtlasStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);
          break;

        case Constants.VECTOR_STORE_PGVECTOR:

          baseStore = new PGVectorStore(storeConfiguration, (PGVectorStoreConnection)storeConnection, storeName, queryParams, dimension, createStore);
          break;

        case Constants.VECTOR_STORE_AI_SEARCH:

          baseStore = new AISearchStore(storeConfiguration, (AISearchStoreConnection)storeConnection, storeName, queryParams, dimension, createStore);
          break;

        case Constants.VECTOR_STORE_CHROMA:

          baseStore = new ChromaStore(storeConfiguration, (ChromaStoreConnection)storeConnection, storeName, queryParams, dimension);
          break;

        case Constants.VECTOR_STORE_PINECONE:

          baseStore = new PineconeStore(storeConfiguration, (PineconeStoreConnection)storeConnection, storeName, queryParams, dimension, createStore);
          break;

        case Constants.VECTOR_STORE_ELASTICSEARCH:

          baseStore = new ElasticsearchStore(storeConfiguration, (ElasticsearchStoreConnection)storeConnection, storeName, queryParams);
          break;

        case Constants.VECTOR_STORE_OPENSEARCH:

          baseStore = new OpenSearchStore(storeConfiguration, (OpenSearchStoreConnection)storeConnection, storeName, queryParams);
          break;

        case Constants.VECTOR_STORE_QDRANT:

          baseStore = new QdrantStore(storeConfiguration, (QdrantStoreConnection)storeConnection, storeName, queryParams, dimension, createStore);
          break;

        case Constants.VECTOR_STORE_ALLOYDB:
          
          baseStore = new AlloyDBStore(storeConfiguration, (AlloyDBStoreConnection)storeConnection, storeName, queryParams, dimension, createStore);
          break;

        case Constants.VECTOR_STORE_WEAVIATE:

          baseStore = new WeaviateStore(storeConfiguration, (WeaviateStoreConnection) storeConnection, storeName, queryParams, dimension);
          break;

        case Constants.VECTOR_STORE_EPHEMERAL_FILE:

          baseStore = new EphemeralFileStore(storeConfiguration, (EphemeralFileStoreConnection)storeConnection, storeName, queryParams, dimension);
          break;

        default:
          throw new ModuleException(
              String.format("Error while initializing embedding store. \"%s\" not supported.", storeConnection.getVectorStore()),
              MuleVectorsErrorType.STORE_SERVICES_FAILURE);
      }
      return baseStore;
    }
  }

  public BaseStore.RowIterator rowIterator() {
    return new BaseStore.RowIterator();
  }

  public class RowIterator implements Iterator<Row<?>> {

    @Override
    public boolean hasNext() {
      throw new UnsupportedOperationException("This method should be overridden by subclasses");
    }

    @Override
    public Row<?> next() {
      throw new UnsupportedOperationException("This method should be overridden by subclasses");
    }
  }

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
