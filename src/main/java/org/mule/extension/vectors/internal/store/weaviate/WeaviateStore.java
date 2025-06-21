package org.mule.extension.vectors.internal.store.weaviate;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;


import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * ChromaStore is a specialized implementation of {@link BaseStoreService} designed to interact with
 * the Chroma database for managing vector data and sources.
 */
public class WeaviateStore extends BaseStoreService {
    private final QueryParameters queryParams;

    /**
     * Initializes a new instance of ChromaStore.
     *
     * @param storeConfiguration the configuration object containing necessary settings.
     * @param storeName          the name of the vector store.
     * @param queryParams        parameters related to query configurations.
     */
    public WeaviateStore(StoreConfiguration storeConfiguration, WeaviateStoreConnection weaviateStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

        super(storeConfiguration, weaviateStoreConnection, storeName, dimension, createStore);

        this.queryParams = queryParams;
    }

    @Override
    public EmbeddingStore<TextSegment> buildEmbeddingStore() {
        return WeaviateEmbeddingStore.builder()
                .apiKey(((WeaviateStoreConnection) storeConnection).getApikey())
                .scheme(((WeaviateStoreConnection) storeConnection).getScheme())
                .host(((WeaviateStoreConnection) storeConnection).getHost())
                .port(((WeaviateStoreConnection) storeConnection).getPort())
                .securedGrpc(((WeaviateStoreConnection) storeConnection).isSecuredGrpc())
                .grpcPort(((WeaviateStoreConnection) storeConnection).getGrpcPort())
                .useGrpcForInserts(((WeaviateStoreConnection) storeConnection).isUseGrpcForInserts())
                .avoidDups(((WeaviateStoreConnection) storeConnection).isAvoidDups())
                .consistencyLevel(((WeaviateStoreConnection) storeConnection).getConsistencyLevel())
                .objectClass(storeName)
                .build();
    }

    @Override
    public Iterator<BaseStoreService.Row<?>> getRowIterator() {
        try {
            return new WeaviateStore.RowIterator();
        } catch (Exception e) {
            LOGGER.error("Error while creating row iterator", e);
            throw new RuntimeException(e);
        }
    }

    public class RowIterator implements Iterator<BaseStoreService.Row<?>> {


        public RowIterator() throws Exception {
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public BaseStoreService.Row<?> next() {
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
