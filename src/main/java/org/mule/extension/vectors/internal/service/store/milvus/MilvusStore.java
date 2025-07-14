package org.mule.extension.vectors.internal.service.store.milvus;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.BaseStoreService;
import org.mule.runtime.extension.api.exception.ModuleException;

public class MilvusStore extends BaseStoreService {

    private final String indexType;
    private final String metricType;
    private final String consistencyLevel;
    private final boolean autoFlushOnInsert;

    private final String idFieldName;
    private final String textFieldName;
    private final String metadataFieldName;
    private final String vectorFieldName;

    private final MilvusServiceClient client;
    private final QueryParameters queryParams;


    public MilvusStore(StoreConfiguration storeConfiguration, MilvusStoreConnection milvusStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

        super(storeConfiguration, milvusStoreConnection, storeName, dimension, createStore);

        this.indexType = milvusStoreConnection.getIndexType();
        this.metricType = milvusStoreConnection.getMetricType();
        this.consistencyLevel = milvusStoreConnection.getConsistencyLevel();
        this.autoFlushOnInsert = milvusStoreConnection.isAutoFlushOnInsert();

        this.idFieldName = milvusStoreConnection.getIdFieldName();
        this.textFieldName = milvusStoreConnection.getTextFieldName();
        this.metadataFieldName = milvusStoreConnection.getMetadataFieldName();
        this.vectorFieldName = milvusStoreConnection.getVectorFieldName();

        this.client = milvusStoreConnection.getClient();
        this.queryParams = queryParams;
    }

    @Override
    public EmbeddingStore<TextSegment> buildEmbeddingStore() {
        try {
            return MilvusEmbeddingStore.builder()
                    .milvusClient(this.client)                                                // Use an existing Milvus client
                    .collectionName(this.storeName)                                           // Name of the collection
                    .dimension(this.dimension)                                                // Dimension of vectors
                    .indexType(IndexType.valueOf(this.indexType))                             // Index type
                    .metricType(MetricType.valueOf(this.metricType))                          // Metric type
                    .consistencyLevel(ConsistencyLevelEnum.valueOf(this.consistencyLevel))    // Consistency level
                    .autoFlushOnInsert(this.autoFlushOnInsert)                                // Auto flush after insert
                    .idFieldName(this.idFieldName)                                            // ID field name
                    .textFieldName(this.textFieldName)                                        // Text field name
                    .metadataFieldName(this.metadataFieldName)                                // Metadata field name
                    .vectorFieldName(this.vectorFieldName)                                    // Vector field name
                    .build();                                                                 // Build the MilvusEmbeddingStore instance
        } catch (Exception e) {
            throw new ModuleException("Failed to build Milvus embedding store: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
        }
    }

    @Override
    public MilvusStoreIterator<?> getFileIterator() {
        return new MilvusStoreIterator<>(
            (MilvusStoreConnection) this.storeConnection, // storeConnection is protected in BaseStoreService
            this.storeName,
            this.queryParams
        );
    }


}
