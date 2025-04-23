package org.mule.extension.vectors.internal.store.alloydb;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;

import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEmbeddingStore;
import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEngine;
import dev.langchain4j.community.store.embedding.alloydb.EmbeddingStoreConfig;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;

public class AlloyDBStore extends BaseStore {
  
  private AlloyDBEngine alloyDBEngine;

  public AlloyDBStore(StoreConfiguration compositeConfiguration, AlloyDBStoreConnection alloyDBStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
    
    super(compositeConfiguration, alloyDBStoreConnection, storeName, queryParams, dimension, createStore);
    this.alloyDBEngine = alloyDBStoreConnection.getAlloyDBEngine();
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    if(createStore) {
      
      EmbeddingStoreConfig embeddingStoreConfig = EmbeddingStoreConfig
      .builder(storeName, dimension)
      .overwriteExisting(false)
      .build();

      this.alloyDBEngine.initVectorStoreTable(embeddingStoreConfig);
    } 

    return new AlloyDBEmbeddingStore.Builder(this.alloyDBEngine, storeName)
        .build();
  }
}
