package org.mule.extension.vectors.internal.store.pinecone;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;

public class PineconeStore extends BaseStore {

  private String apiKey;
  private String cloud;
  private String region;

  public PineconeStore(StoreConfiguration storeConfiguration, PineconeStoreConnection pineconeStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, pineconeStoreConnection, storeName, queryParams, dimension, createStore);

    this.apiKey = pineconeStoreConnection.getApiKey();
    this.cloud = pineconeStoreConnection.getCloud();
    this.region = pineconeStoreConnection.getRegion();
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    return createStore ?

        PineconeEmbeddingStore.builder()
          .apiKey(apiKey)
          .index(storeName)
          .nameSpace("ns0mc_" + storeName)
          .createIndex(PineconeServerlessIndexConfig.builder()
                           .cloud(cloud)
                           .region(region)
                           .dimension(dimension)
                           .build())
          .build():

        PineconeEmbeddingStore.builder()
            .apiKey(apiKey)
            .index(storeName)
            .nameSpace("ns0mc_" + storeName)
            .build();
  }

  @Override
  public PineconeStore.RowIterator rowIterator() {
    try {
      return new PineconeStore.RowIterator();
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

      return true;
    }

    @Override
    public Row<?> next() {
      try {

        return null;

      } catch (Exception e) {
        LOGGER.error("Error while fetching next row", e);
        return null;
      }
    }
  }
}
