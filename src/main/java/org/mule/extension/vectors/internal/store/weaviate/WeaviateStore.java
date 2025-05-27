package org.mule.extension.vectors.internal.store.weaviate;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;

import java.util.NoSuchElementException;

/**
 * ChromaStore is a specialized implementation of {@link BaseStore} designed to interact with
 * the Chroma database for managing vector data and sources.
 */
public class WeaviateStore extends BaseStore {

  /**
   * Initializes a new instance of ChromaStore.
   *
   * @param storeName     the name of the vector store.
   * @param storeConfiguration the configuration object containing necessary settings.
   * @param queryParams   parameters related to query configurations.
   */
  public WeaviateStore(StoreConfiguration storeConfiguration, WeaviateStoreConnection weaviateStoreConnection, String storeName, QueryParameters queryParams, int dimension) {

    super(storeConfiguration, weaviateStoreConnection, storeName, queryParams, dimension, true);


  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {
    return WeaviateEmbeddingStore.builder()
        .apiKey(((WeaviateStoreConnection)storeConnection).getApikey())
        .scheme(((WeaviateStoreConnection)storeConnection).getScheme())
        .host(((WeaviateStoreConnection)storeConnection).getHost())
        .port(((WeaviateStoreConnection)storeConnection).getPort())
        .avoidDups(((WeaviateStoreConnection)storeConnection).isAvoidDups())
        .consistencyLevel(((WeaviateStoreConnection)storeConnection).getConsistencyLevel())
        .build();
  }

  @Override
  public WeaviateStore.RowIterator rowIterator() {
    try {
      return new WeaviateStore.RowIterator();
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
  }

  public class RowIterator extends BaseStore.RowIterator {


    public RowIterator() throws Exception {
      super();
    }

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public Row<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      try {

        return null;

      } catch (Exception e) {
        LOGGER.error("Error while fetching next row", e);
        throw new NoSuchElementException("No more elements available");
      }
    }
  }
}
