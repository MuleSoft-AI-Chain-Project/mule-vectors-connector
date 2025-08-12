package org.mule.extension.vectors.internal.config;

import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.store.ephemeralfile.EphemeralFileStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.store.milvus.MilvusStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.store.mongodbatlas.MongoDBAtlasStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.store.opensearch.OpenSearchStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.store.pgvector.PGVectorStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.store.pinecone.PineconeStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnectionProvider;
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
