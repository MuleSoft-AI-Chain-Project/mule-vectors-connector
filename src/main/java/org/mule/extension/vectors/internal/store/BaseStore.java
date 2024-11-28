package org.mule.extension.vectors.internal.store;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.Configuration;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.aisearch.AISearchStore;
import org.mule.extension.vectors.internal.store.chroma.ChromaStore;
import org.mule.extension.vectors.internal.store.elasticsearch.ElasticsearchStore;
import org.mule.extension.vectors.internal.store.milvus.MilvusStore;
import org.mule.extension.vectors.internal.store.opensearch.OpenSearchStore;
import org.mule.extension.vectors.internal.store.pgvector.PGVectorStore;
import org.mule.extension.vectors.internal.store.pinecone.PineconeStore;
import org.mule.extension.vectors.internal.store.qdrant.QdrantStore;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * The {@code VectorStore} class provides a framework for interacting with various types of vector stores,
 * enabling storage and retrieval of vector embeddings for data analysis and retrieval purposes. It serves as
 * an abstract base for specific implementations such as Milvus, PGVector, and AI Search stores.
 */
public class BaseStore {

  protected static final Logger LOGGER = LoggerFactory.getLogger(BaseStore.class);

  protected String storeName;
  protected Configuration configuration;
  protected QueryParameters queryParams;
  protected int dimension;

  public BaseStore(String storeName, Configuration configuration, QueryParameters queryParams, int dimension) {

    this.storeName = storeName;
    this.configuration = configuration;
    this.queryParams = queryParams;
    this.dimension = dimension;
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  /**
   * Retrieves the embedding model used by this vector store. Initializes the model if it is not already set.
   *
   * @return the embedding model used by the vector store
   */
  public Embedding getZeroVectorEmbedding() {

    // Create a general query vector (e.g., zero vector). Zero vector is often used when you need to retrieve all
    // embeddings without any specific bias.
    float[] queryVector = new float[dimension];
    for (int i = 0; i < dimension; i++) {
      queryVector[i]=0.0f;  // Zero vector
    }
    return new Embedding(queryVector);
  }

  /**
   * Retrieves a JSON object listing sources available in the vector store, including metadata for each source.
   *
   * @return a JSON object containing a list of sources and their metadata
   */
  public JSONObject listSources() {

    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  /**
   * Retrieves a unique key for a given source object by checking specific metadata fields.
   * <p>
   * The method first attempts to retrieve a unique identifier using the source ID (if available).
   * If the source ID is not present, it generates an alternative key by concatenating the
   * {@code fullPath} or {@code url} (whichever is available) with the {@code ingestionDatetime}.
   * </p>
   *
   * @param sourceObject A {@code JSONObject} containing metadata fields for the source. The expected
   *                     keys include {@code METADATA_KEY_SOURCE_ID}, {@code METADATA_KEY_URL},
   *                     {@code METADATA_KEY_FULL_PATH}, and {@code METADATA_KEY_INGESTION_DATETIME}.
   * @return A unique key as a {@code String}. If {@code sourceId} is present, it is returned directly.
   *         Otherwise, the alternative key, based on available fields, is generated and returned.
   *         Returns an empty string if all fields are missing or empty.
   */
  protected String getSourceUniqueKey(JSONObject sourceObject) {

    String sourceId = sourceObject.has(Constants.METADATA_KEY_SOURCE_ID) ? sourceObject.getString(Constants.METADATA_KEY_SOURCE_ID) : "";

    String url = sourceObject.has(Constants.METADATA_KEY_URL) ? sourceObject.getString(Constants.METADATA_KEY_URL) : "";
    String absoluteDirectoryPath = sourceObject.has(Constants.METADATA_KEY_ABSOLUTE_DIRECTORY_PATH) ? sourceObject.getString(Constants.METADATA_KEY_ABSOLUTE_DIRECTORY_PATH) : "";
    String source = sourceObject.has(Constants.METADATA_KEY_SOURCE) ? sourceObject.getString(Constants.METADATA_KEY_SOURCE) : "";
    String ingestionDatetime = sourceObject.has(Constants.METADATA_KEY_INGESTION_DATETIME) ? sourceObject.getString(Constants.METADATA_KEY_INGESTION_DATETIME) : "";

    String alternativeKey =
        ((absoluteDirectoryPath != null && !absoluteDirectoryPath.isEmpty()) ? absoluteDirectoryPath :
            ((url != null && !url.isEmpty()) ? url :
                  (source != null && !source.isEmpty()) ? source : "")) +
              ((ingestionDatetime != null && !ingestionDatetime.isEmpty()) ? ingestionDatetime : "");

    return !sourceId.isEmpty() ? sourceId : alternativeKey;
  }

  /**
   * Adds or updates a source object into the source object map.
   *
   * @param sourceObjectMap The map of source objects keyed by their unique keys.
   * @param sourceObject    The source object to add or update.
   */
  protected void addOrUpdateSourceObjectIntoSourceObjectMap(HashMap<String, JSONObject> sourceObjectMap, JSONObject sourceObject) {

    String sourceUniqueKey = getSourceUniqueKey(sourceObject);

    // Add sourceObject to sources only if it has at least one key-value pair and it's possible to generate a key
    if (!sourceObject.isEmpty() && sourceUniqueKey != null && !sourceUniqueKey.isEmpty()) {
      // Overwrite sourceObject if current one has a greater index (greatest index represents the number of segments)
      if(sourceObjectMap.containsKey(sourceUniqueKey)){
        // Get current index
        int currentSegmentCount = sourceObject.getInt(Constants.JSON_KEY_SEGMENT_COUNT);
        // Get previously stored index
        int storedSegmentCount = (int) sourceObjectMap.get(sourceUniqueKey).get(Constants.JSON_KEY_SEGMENT_COUNT);
        // Check if object need to be updated
        if(currentSegmentCount > storedSegmentCount) {
          sourceObjectMap.put(sourceUniqueKey, sourceObject);
        }
      } else {
        sourceObjectMap.put(sourceUniqueKey, sourceObject);
      }
    }
  }

  /**
   * Extracts and organizes metadata fields from a given JSON object to create a structured source object.
   * <p>
   * The generated source object includes keys such as source ID, file name, URL, full path, and ingestion
   * datetime, among others.
   * </p>
   *
   * @param metadataObject a {@code JSONObject} containing metadata fields.
   * @return a {@code JSONObject} with organized metadata for a source.
   */
  protected JSONObject getSourceObject(JSONObject metadataObject) {

    String sourceId = metadataObject.has(Constants.METADATA_KEY_SOURCE_ID) ?  metadataObject.getString(Constants.METADATA_KEY_SOURCE_ID) : null;
    String index = metadataObject.has(Constants.METADATA_KEY_INDEX) ? metadataObject.getString(Constants.METADATA_KEY_INDEX) : null;
    String fileName = metadataObject.has(Constants.METADATA_KEY_FILE_NAME) ?  metadataObject.getString(Constants.METADATA_KEY_FILE_NAME) : null;
    String url = metadataObject.has(Constants.METADATA_KEY_URL) ?  metadataObject.getString(Constants.METADATA_KEY_URL) : null;
    String title = metadataObject.has(Constants.METADATA_KEY_TITLE) ?  metadataObject.getString(Constants.METADATA_KEY_TITLE) : null;
    String source = metadataObject.has(Constants.METADATA_KEY_SOURCE) ?  metadataObject.getString(Constants.METADATA_KEY_SOURCE) : null;
    String absoluteDirectoryPath = metadataObject.has(Constants.METADATA_KEY_ABSOLUTE_DIRECTORY_PATH) ?  metadataObject.getString(Constants.METADATA_KEY_ABSOLUTE_DIRECTORY_PATH) : null;
    String ingestionDatetime = metadataObject.has(Constants.METADATA_KEY_INGESTION_DATETIME) ?  metadataObject.getString(Constants.METADATA_KEY_INGESTION_DATETIME) : null;
    Long ingestionTimestamp = metadataObject.has(Constants.METADATA_KEY_INGESTION_TIMESTAMP) ?  metadataObject.getLong(Constants.METADATA_KEY_INGESTION_TIMESTAMP) : null;

    JSONObject sourceObject = new JSONObject();
    if(index != null && !index.isEmpty()) sourceObject.put(Constants.JSON_KEY_SEGMENT_COUNT, Integer.parseInt(index) + 1);
    sourceObject.put(Constants.METADATA_KEY_SOURCE_ID, sourceId);
    sourceObject.put(Constants.METADATA_KEY_ABSOLUTE_DIRECTORY_PATH, absoluteDirectoryPath);
    sourceObject.put(Constants.METADATA_KEY_SOURCE, source);
    sourceObject.put(Constants.METADATA_KEY_TITLE, title);
    sourceObject.put(Constants.METADATA_KEY_FILE_NAME, fileName);
    sourceObject.put(Constants.METADATA_KEY_URL, url);
    sourceObject.put(Constants.METADATA_KEY_INGESTION_DATETIME, ingestionDatetime);
    sourceObject.put(Constants.METADATA_KEY_INGESTION_TIMESTAMP, ingestionTimestamp);

    return sourceObject;
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

    private String storeName;
    private Configuration configuration;
    private QueryParameters queryParams;
    private int dimension;

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
     * @param configuration the configuration parameters.
     * @return the {@code Builder} instance, for method chaining.
     */
    public Builder configuration(Configuration configuration) {
      this.configuration = configuration;
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

      LOGGER.debug("Vector Store: " + configuration.getStoreConfiguration().getVectorStore());
      switch (configuration.getStoreConfiguration().getVectorStore()) {

        case Constants.VECTOR_STORE_MILVUS:

          baseStore = new MilvusStore(storeName, configuration, queryParams, dimension);
          break;

        case Constants.VECTOR_STORE_PGVECTOR:

          baseStore = new PGVectorStore(storeName, configuration, queryParams, dimension);
          break;

        case Constants.VECTOR_STORE_AI_SEARCH:

          baseStore = new AISearchStore(storeName, configuration, queryParams, dimension);
          break;

        case Constants.VECTOR_STORE_CHROMA:

          baseStore = new ChromaStore(storeName, configuration, queryParams, dimension);
          break;

        case Constants.VECTOR_STORE_PINECONE:

          baseStore = new PineconeStore(storeName, configuration, queryParams, dimension);
          break;

        case Constants.VECTOR_STORE_ELASTICSEARCH:

          baseStore = new ElasticsearchStore(storeName, configuration, queryParams, dimension);
          break;

        case Constants.VECTOR_STORE_OPENSEARCH:

          baseStore = new OpenSearchStore(storeName, configuration, queryParams, dimension);
          break;

        case Constants.VECTOR_STORE_QDRANT:

          baseStore = new QdrantStore(storeName, configuration, queryParams, dimension);
          break;

        default:
          throw new ModuleException(
              String.format("Error while initializing embedding store. \"%s\" not supported.", configuration.getStoreConfiguration().getVectorStore()),
              MuleVectorsErrorType.STORE_SERVICES_FAILURE);
      }
      return baseStore;
    }
  }
}
