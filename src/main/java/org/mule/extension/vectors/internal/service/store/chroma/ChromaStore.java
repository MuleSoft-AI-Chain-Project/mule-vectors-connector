package org.mule.extension.vectors.internal.service.store.chroma;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.helper.request.HttpRequestHelper;
import org.mule.extension.vectors.internal.service.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.http.api.domain.message.response.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * ChromaStore is a specialized implementation of {@link BaseStoreService} designed to interact with
 * the Chroma database for managing vector data and sources.
 */
public class ChromaStore extends BaseStoreService {

    private final ChromaStoreConnection chromaStoreConnection;
    private final QueryParameters queryParams;

    /**
     * Initializes a new instance of ChromaStore.
     *
     * @param storeConfiguration the configuration object containing necessary settings.
     * @param chromaStoreConnection the connection object for ChromaDB
     * @param storeName          the name of the vector store.
     * @param queryParams        parameters related to query configurations.
     * @param dimension          the dimension of the vectors.
     * @param createStore        flag to create the store if it does not exist.
     */
    public ChromaStore(StoreConfiguration storeConfiguration, ChromaStoreConnection chromaStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {
        super(storeConfiguration, chromaStoreConnection, storeName, dimension, createStore);
        this.chromaStoreConnection = chromaStoreConnection;
        this.queryParams = queryParams;

    }

    @Override
    public EmbeddingStore<TextSegment> buildEmbeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(this.chromaStoreConnection.getUrl())
                .collectionName(this.storeName)
                .build();
    }

    @Override
    public ChromaStoreIterator<?> getFileIterator() {
        return new ChromaStoreIterator<>(
            this.chromaStoreConnection,
            this.storeName,
            this.queryParams
        );
    }

}
