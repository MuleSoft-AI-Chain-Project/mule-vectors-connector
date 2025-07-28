package org.mule.extension.vectors.internal.helper.validation;

import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.alloydb.AlloyDBStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.ephemeralfile.EphemeralFileStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.milvus.MilvusStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.mongodbatlas.MongoDBAtlasStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.opensearch.OpenSearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.pgvector.PGVectorStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.pinecone.PineconeStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.weaviate.WeaviateStoreConnectionParameters;

/**
 * Validation helpers for different connection types.
 * Simplifies parameter validation across all connections.
 */
public class ConnectionValidationStrategies {

  /**
  * Validates AlloyDB connection parameters.
  */
  public static void validateAlloyDB(AlloyDBStoreConnectionParameters params) {
    String connectionType = "AlloyDB";
    ParameterValidator.requireNotBlank(connectionType, "Project ID", params.getProjectId());
    ParameterValidator.requireNotBlank(connectionType, "Region", params.getRegion());
    ParameterValidator.requireNotBlank(connectionType, "Cluster", params.getCluster());
    ParameterValidator.requireNotBlank(connectionType, "Instance", params.getInstance());
    ParameterValidator.requireNotBlank(connectionType, "IAM Account Email", params.getIamAccountEmail());
    ParameterValidator.requireNotBlank(connectionType, "Host", params.getHost());
    ParameterValidator.requirePositive(connectionType, "Port", params.getPort());
    ParameterValidator.requireNotBlank(connectionType, "Database", params.getDatabase());
    ParameterValidator.requireNotBlank(connectionType, "User", params.getUser());
    ParameterValidator.requireNotBlank(connectionType, "Password", params.getPassword());
  }

  /**
  * Validates PGVector connection parameters.
  */
  public static void validatePGVector(PGVectorStoreConnectionParameters params) {
    String connectionType = "PGVector";
    ParameterValidator.requireNotBlank(connectionType, "Host", params.getHost());
    ParameterValidator.requirePositive(connectionType, "Port", params.getPort());
    ParameterValidator.requireNotBlank(connectionType, "Database", params.getDatabase());
    ParameterValidator.requireNotBlank(connectionType, "User", params.getUser());
    ParameterValidator.requireNotBlank(connectionType, "Password", params.getPassword());
  }

  /**
  * Validates Weaviate connection parameters.
  */
  public static void validateWeaviate(WeaviateStoreConnectionParameters params) {
    String connectionType = "Weaviate";
    ParameterValidator.requireNotBlank(connectionType, "Scheme", params.getScheme());
    ParameterValidator.requireNotBlank(connectionType, "Host", params.getHost());
    ParameterValidator.requireNotBlank(connectionType, "API Key", params.getApiKey());
  }

  /**
   * Validates MongoDB Atlas connection parameters.
   */
  public static void validateMongoDBAtlas(MongoDBAtlasStoreConnectionParameters params) {
    String connectionType = "MongoDB Atlas";
    ParameterValidator.requireNotBlank(connectionType, "Host", params.getHost());
    ParameterValidator.requireNotBlank(connectionType, "User", params.getUser());
    ParameterValidator.requireNotBlank(connectionType, "Password", params.getPassword());
    ParameterValidator.requireNotBlank(connectionType, "Database", params.getDatabase());
  }

  /**
  * Validates OpenSearch connection parameters.
  */
  public static void validateOpenSearch(OpenSearchStoreConnectionParameters params) {
    String connectionType = "OpenSearch";
    ParameterValidator.requireNotBlank(connectionType, "URL", params.getUrl());
    ParameterValidator.requireEither(connectionType, "password", params.getPassword(), "API Key", params.getApiKey());
  }

  /**
  * Validates Elasticsearch connection parameters.
  */
  public static void validateElasticsearch(ElasticsearchStoreConnectionParameters params) {
    String connectionType = "Elasticsearch";
    ParameterValidator.requireNotBlank(connectionType, "URL", params.getUrl());
    ParameterValidator.requireEither(connectionType, "password", params.getPassword(), "API Key", params.getApiKey());
  }

  /**
   * Validates Chroma connection parameters.
   */
  public static void validateChroma(ChromaStoreConnectionParameters params) {
    String connectionType = "Chroma";
    ParameterValidator.requireNotBlank(connectionType, "URL", params.getUrl());
  }

  /**
   * Validates Pinecone connection parameters.
   */
  public static void validatePinecone(PineconeStoreConnectionParameters params) {
    String connectionType = "Pinecone";
    ParameterValidator.requireNotBlank(connectionType, "API Key", params.getApiKey());
    ParameterValidator.requireNotBlank(connectionType, "Cloud", params.getCloud());
    ParameterValidator.requireNotBlank(connectionType, "Region", params.getRegion());
  }

  /**
   * Validates Qdrant connection parameters.
   */
  public static void validateQdrant(QdrantStoreConnectionParameters params) {
    String connectionType = "Qdrant";
    ParameterValidator.requireNotBlank(connectionType, "Host", params.getHost());
    ParameterValidator.requirePositive(connectionType, "gprcPort", params.getGprcPort());
    ParameterValidator.requireNotBlank(connectionType, "API Key", params.getApiKey());
    ParameterValidator.requireNotBlank(connectionType, "TextSegmentKey", params.getTextSegmentKey());
  }

  /**
   * Validates Milvus connection parameters.
   */
  public static void validateMilvus(MilvusStoreConnectionParameters params) {
    String connectionType = "Milvus";
    ParameterValidator.requireNotBlank(connectionType, "URI", params.getUri());
    ParameterValidator.requireNotBlank(connectionType, "Token", params.getToken());
  }

  /**
   * Validates AI Search connection parameters.
   */
  public static void validateAISearch(AISearchStoreConnectionParameters params) {
    String connectionType = "AI Search";
    ParameterValidator.requireNotBlank(connectionType, "URL", params.getUrl());
    ParameterValidator.requireNotBlank(connectionType, "API Key", params.getApiKey());
  }

  /**
   * Validates Ephemeral File connection parameters.
   */
  public static void validateEphemeralFile(EphemeralFileStoreConnectionParameters params) {
    String connectionType = "Ephemeral File";
    ParameterValidator.requireNotBlank(connectionType, "Working Directory", params.getWorkingDir());
  }
}
