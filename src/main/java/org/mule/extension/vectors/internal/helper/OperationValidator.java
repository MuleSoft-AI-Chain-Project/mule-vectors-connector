package org.mule.extension.vectors.internal.helper;

import org.mule.extension.vectors.internal.constant.Constants;

import java.util.*;

/**
 * A utility class to validate if a given operation is supported for a specified vector store.
 * <p>
 * This class maintains a mapping between operations (such as "STORE_METADATA", "FILTER_BY_METADATA", "REMOVE_EMBEDDINGS")
 * and the vector stores that support those operations. It is used to ensure that only supported operations are
 * performed on a particular vector store.
 * </p>
 *
 * For more details, refer to the LangChain4j documentation:
 * {@link https://docs.langchain4j.dev/integrations/embedding-stores/}
 *
 * <p>
 * Supported vector stores for each operation are stored in a static map:
 * <pre>
 *   EMBEDDING_OPERATION_TYPE_TO_SUPPORTED_VECTOR_STORES.put("STORE_METADATA", new HashSet<>(Arrays.asList(
 *       Constants.VECTOR_STORE_PGVECTOR,
 *       Constants.VECTOR_STORE_ELASTICSEARCH,
 *       Constants.VECTOR_STORE_MILVUS,
 *       Constants.VECTOR_STORE_CHROMA,
 *       Constants.VECTOR_STORE_PINECONE,
 *       Constants.VECTOR_STORE_AI_SEARCH
 *   )));
 * </pre>
 * </p>
 *
 * @see Constants
 */
public class OperationValidator {

  private static final Map<String, Set<String>> EMBEDDING_OPERATION_TYPE_TO_SUPPORTED_VECTOR_STORES =
          new HashMap<>();

  static {
    // Mapping operation types to supported vector stores
    EMBEDDING_OPERATION_TYPE_TO_SUPPORTED_VECTOR_STORES.put(Constants.STORE_OPERATION_TYPE_STORE_METADATA,
            new HashSet<>(Arrays.asList(
              Constants.VECTOR_STORE_ALLOYDB,
              Constants.VECTOR_STORE_PGVECTOR,
              Constants.VECTOR_STORE_ELASTICSEARCH,
              Constants.VECTOR_STORE_OPENSEARCH,
              Constants.VECTOR_STORE_MILVUS,
              Constants.VECTOR_STORE_MONGODB_ATLAS,
              Constants.VECTOR_STORE_CHROMA,
              Constants.VECTOR_STORE_PINECONE,
              Constants.VECTOR_STORE_AI_SEARCH,
              Constants.VECTOR_STORE_QDRANT,
              Constants.VECTOR_STORE_EPHEMERAL_FILE
            )));

    EMBEDDING_OPERATION_TYPE_TO_SUPPORTED_VECTOR_STORES.put(Constants.STORE_OPERATION_TYPE_FILTER_BY_METADATA,
            new HashSet<>(Arrays.asList(
              Constants.VECTOR_STORE_ALLOYDB,
              Constants.VECTOR_STORE_PGVECTOR,
              Constants.VECTOR_STORE_ELASTICSEARCH,
              Constants.VECTOR_STORE_OPENSEARCH,
              Constants.VECTOR_STORE_MILVUS,
              Constants.VECTOR_STORE_MONGODB_ATLAS,
              Constants.VECTOR_STORE_CHROMA,
              Constants.VECTOR_STORE_PINECONE, // Do not support GTE with strings.
              Constants.VECTOR_STORE_AI_SEARCH,
              Constants.VECTOR_STORE_QDRANT,
              Constants.VECTOR_STORE_EPHEMERAL_FILE
            )));

    EMBEDDING_OPERATION_TYPE_TO_SUPPORTED_VECTOR_STORES.put(Constants.STORE_OPERATION_TYPE_REMOVE_EMBEDDINGS,
            new HashSet<>(Arrays.asList(
              Constants.VECTOR_STORE_ALLOYDB,
              Constants.VECTOR_STORE_PGVECTOR,
              Constants.VECTOR_STORE_ELASTICSEARCH,
              // Constants.VECTOR_STORE_OPENSEARCH, // Not supported yet.
              Constants.VECTOR_STORE_MILVUS,
              Constants.VECTOR_STORE_MONGODB_ATLAS,
              Constants.VECTOR_STORE_CHROMA,
              // Constants.VECTOR_STORE_PINECONE,
              Constants.VECTOR_STORE_AI_SEARCH,
              Constants.VECTOR_STORE_EPHEMERAL_FILE
            )));

    EMBEDDING_OPERATION_TYPE_TO_SUPPORTED_VECTOR_STORES.put(Constants.STORE_OPERATION_TYPE_QUERY_ALL,
            new HashSet<>(Arrays.asList(
              Constants.VECTOR_STORE_ALLOYDB,
              Constants.VECTOR_STORE_PGVECTOR,
              Constants.VECTOR_STORE_ELASTICSEARCH,
              Constants.VECTOR_STORE_OPENSEARCH,
              Constants.VECTOR_STORE_MILVUS,
              Constants.VECTOR_STORE_MONGODB_ATLAS,
              Constants.VECTOR_STORE_CHROMA,
              Constants.VECTOR_STORE_PINECONE,
              Constants.VECTOR_STORE_AI_SEARCH,
              Constants.VECTOR_STORE_QDRANT,
              Constants.VECTOR_STORE_EPHEMERAL_FILE
            )));

  }

  /**
   * Validates whether a given operationType is supported for a specific vector store.
   *
   * @param operationType the operationType to validate (e.g., "STORE_METADATA", "FILTER_BY_METADATA", "REMOVE_EMBEDDINGS")
   * @param vectorStore the name of the vector store to validate against
   * @throws UnsupportedOperationException if the operationType is not supported for the given vector store
   *
   * @throws IllegalArgumentException if the operationType or vectorStore is null or empty
   */
  public static void validateOperationType(String operationType, String vectorStore) {

    // Validate inputs
    if (operationType == null || operationType.isEmpty() || vectorStore == null || vectorStore.isEmpty()) {
      throw new IllegalArgumentException("Operation and vectorStore cannot be null or empty");
    }

    // Retrieve supported vector stores for the operationType
    Set<String> supportedVectorStores = EMBEDDING_OPERATION_TYPE_TO_SUPPORTED_VECTOR_STORES.get(operationType);

    // Check if the operationType is supported for the given vector store
    if (supportedVectorStores == null || !supportedVectorStores.contains(vectorStore)) {
      throw new UnsupportedOperationException("Operation " + operationType + " is not supported by " + vectorStore);
    }
  }
}
