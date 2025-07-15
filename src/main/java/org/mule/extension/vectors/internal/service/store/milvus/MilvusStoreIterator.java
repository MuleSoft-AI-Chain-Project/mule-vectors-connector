package org.mule.extension.vectors.internal.service.store.milvus;
import org.mule.extension.vectors.internal.connection.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.milvus.client.MilvusServiceClient;
import io.milvus.exception.MilvusException;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.param.dml.QueryIteratorParam;
import io.milvus.param.R;
import io.grpc.StatusRuntimeException;

import java.util.*;

public class MilvusStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MilvusStoreIterator.class);

  private final MilvusServiceClient client;
  private final QueryParameters queryParams;
  private final String idFieldName;
  private final String textFieldName;
  private final String metadataFieldName;
  private final String vectorFieldName;

  private QueryIterator queryIterator;
  private List<io.milvus.response.QueryResultsWrapper.RowRecord> currentBatch;
  private int currentIndex;

  public MilvusStoreIterator(
      MilvusStoreConnection milvusStoreConnection,
      String storeName,
      QueryParameters queryParams
  ) {
    this.client = milvusStoreConnection.getClient();
    this.queryParams = queryParams;
    this.idFieldName = milvusStoreConnection.getIdFieldName();
    this.textFieldName = milvusStoreConnection.getTextFieldName();
    this.metadataFieldName = milvusStoreConnection.getMetadataFieldName();
    this.vectorFieldName = milvusStoreConnection.getVectorFieldName();

    List<String> outFields = queryParams.retrieveEmbeddings() ?
        Arrays.asList(idFieldName, vectorFieldName, textFieldName, metadataFieldName) :
        Arrays.asList(idFieldName, textFieldName, metadataFieldName);

    QueryIteratorParam iteratorParam = QueryIteratorParam.newBuilder()
        .withCollectionName(storeName)
        .withBatchSize((long) queryParams.pageSize())
        .withOutFields(outFields)
        .build();

    R<QueryIterator> queryIteratorRes = client.queryIterator(iteratorParam);
    if (queryIteratorRes.getStatus() != R.Status.Success.getCode()) {
      throw new MilvusException(queryIteratorRes.getMessage(), queryIteratorRes.getStatus());
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
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    io.milvus.response.QueryResultsWrapper.RowRecord rowRecord = currentBatch.get(currentIndex++);
    String embeddingId = (String) rowRecord.getFieldValues().get(idFieldName);
    float[] vector = null;
    if (queryParams.retrieveEmbeddings()) {
      List<Float> vectorList = (List<Float>) rowRecord.getFieldValues().get(vectorFieldName);
      vector = new float[vectorList.size()];
      for (int i = 0; i < vectorList.size(); i++) {
        vector[i] = vectorList.get(i);
      }
    }
    String text = (String) rowRecord.getFieldValues().get(textFieldName);
    Object metadataRaw = rowRecord.getFieldValues().get(metadataFieldName);
    JSONObject metadataObject = new JSONObject(metadataRaw.toString());

    // This is the only place you may want to adapt for Embedded type.
    // If you want to keep it generic, you can cast or use a factory.
    // For now, we keep it as TextSegment to match the original.
    @SuppressWarnings("unchecked")
    Embedded embedded = (Embedded) new TextSegment(text, Metadata.from(metadataObject.toMap()));

    return new VectorStoreRow<>(embeddingId,
                                vector != null ? new Embedding(vector) : null,
                                embedded);
  }

  private boolean fetchNextBatch() {
    if (queryIterator == null) {
      return false;
    }
    try {
      currentBatch = queryIterator.next();
      currentIndex = 0;
      if (currentBatch.isEmpty()) {
        queryIterator.close();
        queryIterator = null;
        return false;
      }
      return true;
    } catch (StatusRuntimeException e) {
      LOGGER.error("gRPC error fetching next Milvus batch", e);
      switch (e.getStatus().getCode()) {
        case UNAUTHENTICATED:
          throw new ModuleException("Authentication failed: " + e.getStatus().getDescription(), MuleVectorsErrorType.AUTHENTICATION, e);
        case INVALID_ARGUMENT:
          throw new ModuleException("Invalid request to Milvus: " + e.getStatus().getDescription(), MuleVectorsErrorType.INVALID_REQUEST, e);
        default:
          throw new ModuleException("Milvus service error: " + e.getStatus().getDescription(), MuleVectorsErrorType.SERVICE_ERROR, e);
      }
    } catch (MilvusException e) {
      throw new ModuleException("Milvus error: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }
}
