/**
 * (c) 2003-2024 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.vectors.internal.constant;

public class Constants {

  private Constants() {}

  public static final String FILE_TYPE_ANY = "any";
  public static final String FILE_TYPE_TEXT = "text";
  public static final String FILE_TYPE_CRAWL = "crawl";
  public static final String FILE_TYPE_URL = "url";
  public static final String FILE_TYPE_PNG = "png";
  public static final String FILE_TYPE_BMP = "bmp";
  public static final String FILE_TYPE_JPEG = "jpeg";
  public static final String FILE_TYPE_GIF = "gif";

  public static final String MEDIA_TYPE_IMAGE = "image";
  public static final String MEDIA_TYPE_VIDEO = "video";

  public static final String STORAGE_TYPE_LOCAL = "Local";
  public static final String STORAGE_TYPE_AWS_S3 = "S3";
  public static final String STORAGE_TYPE_AZURE_BLOB = "AZURE_BLOB";
  public static final String STORAGE_TYPE_GCS = "GCS";

  public static final String GCS_PREFIX = "gs://";

  public static final String GCP_AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
  public static final String GCP_TOKEN_URI = "https://oauth2.googleapis.com/token";
  public static final String GCP_AUTH_PROVIDER_X509_CERT_URL = "https://www.googleapis.com/oauth2/v1/certs";
  public static final String GCP_CLIENT_X509_CERT_URL = "https://www.googleapis.com/robot/v1/metadata/x509/";

  public static final String EMBEDDING_MODEL_SERVICE_OPENAI = "OPENAI";
  public static final String EMBEDDING_MODEL_SERVICE_AZURE_AI_VISION = "AZURE_AI_VISION";
  public static final String EMBEDDING_MODEL_SERVICE_AZURE_OPENAI = "AZURE_OPENAI";
  public static final String EMBEDDING_MODEL_SERVICE_MISTRAL_AI = "MISTRAL_AI";
  public static final String EMBEDDING_MODEL_SERVICE_NOMIC = "NOMIC";
  public static final String EMBEDDING_MODEL_SERVICE_OLLAMA = "OLLAMA";
  public static final String EMBEDDING_MODEL_SERVICE_HUGGING_FACE = "HUGGING_FACE";
  public static final String EMBEDDING_MODEL_SERVICE_EINSTEIN = "EINSTEIN";
  public static final String EMBEDDING_MODEL_SERVICE_VERTEX_AI = "VERTEX_AI";

  public static final String VECTOR_STORE_PGVECTOR = "PGVECTOR";
  public static final String VECTOR_STORE_ELASTICSEARCH = "ELASTICSEARCH";

  public static final String VECTOR_STORE_OPENSEARCH = "OPENSEARCH";
  public static final String VECTOR_STORE_MILVUS = "MILVUS";
  public static final String VECTOR_STORE_CHROMA = "CHROMA";
  public static final String VECTOR_STORE_PINECONE = "PINECONE";
  public static final String VECTOR_STORE_AI_SEARCH = "AI_SEARCH";
  public static final String VECTOR_STORE_QDRANT = "QDRANT";

  public static final String STORE_SCHEMA_METADATA_FIELD_NAME = "metadata";
  public static final String STORE_SCHEMA_VECTOR_FIELD_NAME = "vector";

  public static final String METADATA_KEY_SOURCE_ID = "source_id";
  public static final String METADATA_KEY_INDEX = "index";
  public static final String METADATA_KEY_FILE_NAME = "file_name";
  public static final String METADATA_KEY_FILE_TYPE = "file_type";
  public static final String METADATA_KEY_MEDIA_TYPE = "media_type";
  public static final String METADATA_KEY_MIME_TYPE = "mime_type";
  public static final String METADATA_KEY_ABSOLUTE_DIRECTORY_PATH = "absolute_directory_path";
  public static final String METADATA_KEY_URL = "url";
  public static final String METADATA_KEY_SOURCE = "source";
  public static final String METADATA_KEY_TITLE = "title";
  public static final String METADATA_KEY_INGESTION_DATETIME = "ingestion_datetime";
  public static final String METADATA_KEY_INGESTION_TIMESTAMP = "ingestion_timestamp";

  public static final String METADATA_FILTER_METHOD_IS_EQUAL_TO = "isEqualTo";
  public static final String METADATA_FILTER_METHOD_IS_NOT_EQUAL_TO = "isNotEqualTo";
  public static final String METADATA_FILTER_METHOD_IS_GREATER_THAN = "isGreaterThan";
  public static final String METADATA_FILTER_METHOD_IS_GREATER_THAN_OR_EQUAL_TO = "isGreaterThanOrEqualTo";
  public static final String METADATA_FILTER_METHOD_IS_LESS_THAN = "isLessThan";
  public static final String METADATA_FILTER_METHOD_IS_LESS_THAN_OR_EQUAL_TO = "isLessThanOrEqualTo";

  public static final Double EMBEDDING_SEARCH_REQUEST_DEFAULT_MIN_SCORE = 0.7;

  public static final String STORE_OPERATION_TYPE_STORE_METADATA = "STORE_METADATA";
  public static final String STORE_OPERATION_TYPE_FILTER_BY_METADATA = "FILTER_BY_METADATA";
  public static final String STORE_OPERATION_TYPE_REMOVE_EMBEDDINGS = "REMOVE_EMBEDDINGS";
  public static final String STORE_OPERATION_TYPE_QUERY_ALL = "QUERY_ALL";

  public static final String JSON_KEY_SOURCES = "sources";
  public static final String JSON_KEY_TEXT_SEGMENTS = "text-segments";
  public static final String JSON_KEY_SEGMENT_COUNT = "segmentCount";
  public static final String JSON_KEY_SOURCE_COUNT = "sourceCount";
  public static final String JSON_KEY_STORE_NAME = "storeName";
  public static final String JSON_KEY_TEXT = "text";
  public static final String JSON_KEY_STATUS = "status";
  public static final String JSON_KEY_EMBEDDINGS = "embeddings";
  public static final String JSON_KEY_DIMENSION = "dimension";
  public static final String JSON_KEY_RESPONSE = "response";
  public static final String JSON_KEY_QUESTION = "question";
  public static final String JSON_KEY_MAX_RESULTS = "maxResults";
  public static final String JSON_KEY_MIN_SCORE = "minScore";
  public static final String JSON_KEY_EMBEDDING_ID = "embeddingId";
  public static final String JSON_KEY_SCORE = "score";
  public static final String JSON_KEY_METADATA = "metadata";
  public static final String JSON_KEY_INDEX = "index";
  public static final String JSON_KEY_BASE64DATA = "base64Data";

  public static final String OPERATION_STATUS_UPDATED = "updated";
  public static final String OPERATION_STATUS_DELETED = "deleted";

  public static final String PARAM_DISPLAY_NAME_STORAGE_OVERRIDE = "Storage (Override Module Configuration)";

  public static final String URI_HTTPS_PREFIX = "https://";

  public static final String HTTP_METHOD_POST = "POST";
}
