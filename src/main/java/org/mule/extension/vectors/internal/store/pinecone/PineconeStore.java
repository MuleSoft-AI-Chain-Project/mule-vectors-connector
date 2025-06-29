package org.mule.extension.vectors.internal.store.pinecone;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PineconeStore extends BaseStoreService {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PineconeStore.class);

    private final String apiKey;
    private final String cloud;
    private final String region;
    private final QueryParameters queryParams;

    public PineconeStore(StoreConfiguration storeConfiguration, PineconeStoreConnection pineconeStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

        super(storeConfiguration, pineconeStoreConnection, storeName, dimension, createStore);

        if (queryParams != null) ValidationUtils.ensureBetween(queryParams.pageSize(), 0, 100, "pageSize");
        this.apiKey = pineconeStoreConnection.getApiKey();
        this.cloud = pineconeStoreConnection.getCloud();
        this.region = pineconeStoreConnection.getRegion();
        this.queryParams = queryParams;
    }

    @Override
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
                        .build() :

                PineconeEmbeddingStore.builder()
                        .apiKey(apiKey)
                        .index(storeName)
                        .nameSpace("ns0mc_" + storeName)
                        .build();
    }


}
