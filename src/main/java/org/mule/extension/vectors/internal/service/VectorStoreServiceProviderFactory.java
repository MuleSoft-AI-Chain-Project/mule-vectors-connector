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
import org.mule.extension.vectors.internal.store.aisearch.AISearchStoreServiceProvider;
import org.mule.extension.vectors.internal.store.alloydb.AlloyDBStoreServiceProvider;
import org.mule.extension.vectors.internal.store.chroma.ChromaStoreServiceProvider;
import org.mule.extension.vectors.internal.store.elasticsearch.ElasticsearchStoreServiceProvider;
import org.mule.extension.vectors.internal.store.ephemeralfile.EphemeralFileStoreServiceProvider;
import org.mule.extension.vectors.internal.store.milvus.MilvusStoreServiceProvider;
import org.mule.extension.vectors.internal.store.mongodbatlas.MongoDBAtlasStoreServiceProvider;
import org.mule.extension.vectors.internal.store.opensearch.OpenSearchStoreServiceProvider;
import org.mule.extension.vectors.internal.store.pgvector.PGVectorStoreServiceProvider;
import org.mule.extension.vectors.internal.store.pinecone.PineconeStoreServiceProvider;
import org.mule.extension.vectors.internal.store.qdrant.QdrantStoreServiceProvider;
import org.mule.extension.vectors.internal.store.weaviate.WeaviateStoreServiceProvider;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.concurrent.ExecutionException;

public class VectorStoreServiceProviderFactory {
  public static VectorStoreServiceProvider getInstance(StoreConfiguration storeConfiguration,
                                               BaseStoreConnection storeConnection,
                                               String storeName,
                                               QueryParameters queryParams,
                                               int dimension,
                                               boolean createStore) throws ExecutionException, InterruptedException {

    switch (storeConnection.getVectorStore()) {



      case Constants.VECTOR_STORE_AI_SEARCH:
        return new AISearchStoreServiceProvider(storeConfiguration, (AISearchStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_ALLOYDB:
        return new AlloyDBStoreServiceProvider(storeConfiguration, (AlloyDBStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_CHROMA:
        return new ChromaStoreServiceProvider(storeConfiguration, (ChromaStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_ELASTICSEARCH:
        return new ElasticsearchStoreServiceProvider(storeConfiguration, (ElasticsearchStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_PGVECTOR:
        return new PGVectorStoreServiceProvider(storeConfiguration, (PGVectorStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_MILVUS:
        return new MilvusStoreServiceProvider(storeConfiguration, (MilvusStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_OPENSEARCH:
        return new OpenSearchStoreServiceProvider(storeConfiguration, (OpenSearchStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_PINECONE:
        return new PineconeStoreServiceProvider(storeConfiguration, (PineconeStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_QDRANT:
        return new QdrantStoreServiceProvider(storeConfiguration, (QdrantStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_WEAVIATE:
        return new WeaviateStoreServiceProvider(storeConfiguration, (WeaviateStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_EPHEMERAL_FILE:
        return new EphemeralFileStoreServiceProvider(storeConfiguration, (EphemeralFileStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);

      case Constants.VECTOR_STORE_MONGODB_ATLAS:
        return new MongoDBAtlasStoreServiceProvider(storeConfiguration, (MongoDBAtlasStoreConnection) storeConnection, storeName, queryParams, dimension, createStore);


      default:
        throw new ModuleException(
            String.format("Error while initializing embedding store. \"%s\" not supported.", storeConnection.getVectorStore()),
            MuleVectorsErrorType.STORE_SERVICES_FAILURE);
    }
  }
}
