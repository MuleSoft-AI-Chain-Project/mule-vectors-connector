package org.mule.extension.vectors.internal.config;

import org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.chroma.ChromaStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.mongodbatlas.MongoDBAtlasStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.opensearch.OpenSearchStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.pgvector.PGVectorStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.qdrant.QdrantStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnectionProvider;
import org.mule.extension.vectors.internal.operation.StoreOperations;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;

@org.mule.runtime.extension.api.annotation.Configuration(name = "storeConfig")
@ConnectionProviders({
    AISearchStoreConnectionProvider.class,
    ChromaStoreConnectionProvider.class,
    ElasticsearchStoreConnectionProvider.class,
    MilvusStoreConnectionProvider.class,
    MongoDBAtlasStoreConnectionProvider.class,
    OpenSearchStoreConnectionProvider.class,
    PGVectorStoreConnectionProvider.class,
    PineconeStoreConnectionProvider.class,
    QdrantStoreConnectionProvider.class,
    EphemeralFileStoreConnectionProvider.class})
@Operations({StoreOperations.class})
public class StoreConfiguration {

}
