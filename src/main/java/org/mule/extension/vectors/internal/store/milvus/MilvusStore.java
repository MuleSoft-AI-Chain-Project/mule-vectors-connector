package org.mule.extension.vectors.internal.store.milvus;

import com.google.gson.JsonObject;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.dml.QueryIteratorParam;
import io.milvus.param.R;
import io.milvus.response.QueryResultsWrapper;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;

import java.util.*;

public class MilvusStore extends BaseStore {

  private String indexType;
  private String metricType;
  private String consistencyLevel;
  private boolean autoFlushOnInsert;

  private String idFieldName;
  private String textFieldName;
  private String metadataFieldName;
  private String vectorFieldName;

  private MilvusServiceClient client;

  public MilvusStore(StoreConfiguration storeConfiguration, MilvusStoreConnection milvusStoreConnection, String storeName, QueryParameters queryParams, int dimension) {

    super(storeConfiguration, milvusStoreConnection, storeName, queryParams, dimension, true);

    this.indexType = milvusStoreConnection.getIndexType();
    this.metricType = milvusStoreConnection.getMetricType();
    this.consistencyLevel = milvusStoreConnection.getConsistencyLevel();
    this.autoFlushOnInsert = milvusStoreConnection.isAutoFlushOnInsert();

    this.idFieldName = milvusStoreConnection.getIdFieldName();
    this.textFieldName = milvusStoreConnection.getTextFieldName();
    this.metadataFieldName = milvusStoreConnection.getMetadataFieldName();
    this.vectorFieldName = milvusStoreConnection.getVectorFieldName();

    this.client = milvusStoreConnection.getClient();
  }

  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

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
  }

  @Override
  public MilvusStore.RowIterator rowIterator() {
    try {
      return new MilvusStore.RowIterator();
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
  }

  public class RowIterator extends BaseStore.RowIterator {

    private QueryIterator queryIterator;
    private List<QueryResultsWrapper.RowRecord> currentBatch;
    private int currentIndex;

    public RowIterator() throws Exception {
        super();

      List<String> outFields = queryParams.retrieveEmbeddings() ?
          Arrays.asList(idFieldName,
                        vectorFieldName,
                        textFieldName,
                        metadataFieldName) :
          Arrays.asList(idFieldName,
                        textFieldName,
                        metadataFieldName);

        QueryIteratorParam iteratorParam = QueryIteratorParam.newBuilder()
            .withCollectionName(storeName)
            .withBatchSize((long) queryParams.pageSize())
            .withOutFields(outFields)
            .build();

        R<QueryIterator> queryIteratorRes = client.queryIterator(iteratorParam);
        if (queryIteratorRes.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException(queryIteratorRes.getMessage());
        }

        this.queryIterator = queryIteratorRes.getData();
        this.currentBatch = new ArrayList<>();
        this.currentIndex = 0;
        fetchNextBatch();
    }

    @Override
    public boolean hasNext() {
      return currentIndex < currentBatch.size() || fetchNextBatch();
    }

    @Override
    public Row<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      QueryResultsWrapper.RowRecord rowRecord = currentBatch.get(currentIndex++);
      String embeddingId = (String)rowRecord.getFieldValues().get(idFieldName);
      float[] vector = null;
      if(queryParams.retrieveEmbeddings()) {
        List<Float> vectorList = (List<Float>) rowRecord.getFieldValues().get(vectorFieldName);
        vector = new float[vectorList.size()];
        for (int i = 0; i < vectorList.size(); i++) {
          vector[i] = vectorList.get(i).floatValue();
        }
      }
      String text = (String)rowRecord.getFieldValues().get(textFieldName);
      Object metadataRaw = rowRecord.getFieldValues().get(metadataFieldName);
      JSONObject metadataObject = new JSONObject(metadataRaw.toString());

      return new Row<TextSegment>(embeddingId,
                                  vector != null ? new Embedding(vector) : null,
                                  new TextSegment(text, Metadata.from(metadataObject.toMap())));
    }

    private boolean fetchNextBatch() {
      if (queryIterator == null) {
        return false;
      }
      currentBatch = queryIterator.next();
      currentIndex = 0;
      if (currentBatch.isEmpty()) {
        queryIterator.close();
        queryIterator = null;
        return false;
      }
      return true;
    }
  }
}
