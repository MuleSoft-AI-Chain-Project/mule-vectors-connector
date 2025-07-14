package org.mule.extension.vectors.internal.service;

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
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.aisearch.AISearchStore;
import org.mule.extension.vectors.internal.store.chroma.ChromaStore;
import org.mule.extension.vectors.internal.store.elasticsearch.ElasticsearchStore;
import org.mule.extension.vectors.internal.store.milvus.MilvusStore;
import org.mule.extension.vectors.internal.store.mongodbatlas.MongoDBAtlasStore;
import org.mule.extension.vectors.internal.store.opensearch.OpenSearchStore;
import org.mule.extension.vectors.internal.store.pgvector.PGVectorStore;
import org.mule.extension.vectors.internal.store.pinecone.PineconeStore;
import org.mule.extension.vectors.internal.store.qdrant.QdrantStore;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.concurrent.ExecutionException;

public class VectorStoreServiceProviderFactory {

  public static VectorStoreService getService(StoreConfiguration storeConfiguration,
                                               BaseStoreConnection storeConnection,
                                               String storeName,
                                               QueryParameters queryParams,
                                               int dimension,
                                               boolean createStore) throws ExecutionException, InterruptedException {
    switch (storeConnection.getVectorStore()) {
      case Constants.VECTOR_STORE_MILVUS:
        return new MilvusStore(
            storeConfiguration,
            (MilvusStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_MONGODB_ATLAS:
        return new MongoDBAtlasStore(
            storeConfiguration,
            (MongoDBAtlasStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_PGVECTOR:
        return new PGVectorStore(
            storeConfiguration,
            (PGVectorStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_AI_SEARCH:
        return new AISearchStore(
            storeConfiguration,
            (AISearchStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_CHROMA:
        return new ChromaStore(
            storeConfiguration,
            (ChromaStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_PINECONE:
        return new PineconeStore(
            storeConfiguration,
            (PineconeStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_ELASTICSEARCH:
        return new ElasticsearchStore(
            storeConfiguration,
            (ElasticsearchStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_OPENSEARCH:
        return new OpenSearchStore(
            storeConfiguration,
            (OpenSearchStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_QDRANT:
        return new QdrantStore(
            storeConfiguration,
            (QdrantStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_ALLOYDB:
        return new org.mule.extension.vectors.internal.store.alloydb.AlloyDBStore(
            storeConfiguration,
            (org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_EPHEMERAL_FILE:
        return new org.mule.extension.vectors.internal.store.ephemeralfile.EphemeralFileStore(
            storeConfiguration,
            (org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      case Constants.VECTOR_STORE_WEAVIATE:
        return new org.mule.extension.vectors.internal.store.weaviate.WeaviateStore(
            storeConfiguration,
            (org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnection) storeConnection,
            storeName,
            queryParams,
            dimension,
            createStore);
      default:
        throw new ModuleException(
            String.format("Error while initializing vector store service. \"%s\" not supported.", storeConnection.getVectorStore()),
            MuleVectorsErrorType.STORE_SERVICES_FAILURE);
    }
  }
}
