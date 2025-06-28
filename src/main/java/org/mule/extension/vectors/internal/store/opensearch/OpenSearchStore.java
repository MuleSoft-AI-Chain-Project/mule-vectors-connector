package org.mule.extension.vectors.internal.store.opensearch;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.opensearch.client.opensearch.OpenSearchClient;


public class OpenSearchStore extends BaseStoreService {


    private final String url;
    private final OpenSearchClient openSearchClient;
    private final QueryParameters queryParams;

    public OpenSearchStore(StoreConfiguration storeConfiguration, OpenSearchStoreConnection openSearchStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

        super(storeConfiguration, openSearchStoreConnection, storeName, dimension, createStore);

        this.url = openSearchStoreConnection.getUrl();
        this.openSearchClient = openSearchStoreConnection.getOpenSearchClient();
        this.queryParams = queryParams;
    }

    @Override
    public EmbeddingStore<TextSegment> buildEmbeddingStore() {
        try {
            return OpenSearchEmbeddingStore.builder()
                    .serverUrl(url)
                    .openSearchClient(openSearchClient)
                    .indexName(storeName)
                    .build();
        } catch (Exception e) {
            throw new ModuleException("Failed to build OpenSearch embedding store: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
        }
    }


}
