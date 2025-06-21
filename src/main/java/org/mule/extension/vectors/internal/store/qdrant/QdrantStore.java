package org.mule.extension.vectors.internal.store.qdrant;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import org.json.JSONObject;
import com.google.protobuf.Value;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;

import org.mule.extension.vectors.internal.store.BaseStoreService;
import org.mule.extension.vectors.internal.util.JsonUtils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class QdrantStore extends BaseStoreService {

  static final String ID_DEFAULT_FIELD_NAME = "embedding_id";
  static final String METADATA_DEFAULT_FIELD_NAME = "metadata";
  static final String VECTOR_DEFAULT_FIELD_NAME = "embedding";

  private static final Logger LOGGER = LoggerFactory.getLogger(QdrantStore.class);

  private final String payloadTextKey;
  private final QdrantClient client;
  private final QueryParameters queryParams;

  public QdrantStore(StoreConfiguration storeConfiguration, QdrantStoreConnection qdrantStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, qdrantStoreConnection, storeName, dimension, createStore);

    try {

      String host = qdrantStoreConnection.getHost();
      String apiKey = qdrantStoreConnection.getApiKey();
      int port = qdrantStoreConnection.getGprcPort();
      boolean useTls = qdrantStoreConnection.isUseTLS();
      this.client = qdrantStoreConnection.getClient();
      this.queryParams = queryParams;

      if (createStore && !this.client.collectionExistsAsync(this.storeName).get() && dimension > 0) {

        qdrantStoreConnection.createCollection(storeName, dimension);
      }
      this.payloadTextKey = qdrantStoreConnection.getTextSegmentKey();

    } catch (Exception e) {

      throw new ModuleException(
          String.format("Error while initializing embedding store \"%s\".", qdrantStoreConnection.getVectorStore()),
          MuleVectorsErrorType.STORE_SERVICES_FAILURE);
    }
  }

  @Override
  public EmbeddingStore<TextSegment> buildEmbeddingStore() {

    return QdrantEmbeddingStore.builder()
        .client(client)
        .payloadTextKey(payloadTextKey)
        .collectionName(storeName)
        .build();
  }

  @Override
  public Iterator<BaseStoreService.Row<?>> getRowIterator() {
    try {
      return new QdrantStore.RowIterator();
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
  }

  public class RowIterator implements Iterator<BaseStoreService.Row<?>> {

    private Iterator<Points.RetrievedPoint> pointIterator;
    private Points.PointId nextOffset;
    private boolean hasMorePages = true;

    public RowIterator() {
      this.pointIterator = new ArrayList<Points.RetrievedPoint>().iterator();
    }

    @Override
    public boolean hasNext() {
      if (!pointIterator.hasNext() && hasMorePages) {
        fetchNextPage();
      }
      return pointIterator.hasNext();
    }

    @Override
    public BaseStoreService.Row<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      try {
        Points.RetrievedPoint currentPoint = pointIterator.next();
        String id = currentPoint.getId().getUuid();
        String text = currentPoint.getPayloadOrDefault(payloadTextKey,
                                                       io.qdrant.client.grpc.JsonWithInt.Value.getDefaultInstance()).getStringValue();
        JSONObject metadataObject = new JSONObject(JsonFactory.toJson(currentPoint.getPayloadMap()));

        // Convert BigDecimal values to a supported type
        Map<String, Object> metadataMap = metadataObject.toMap();
        metadataMap.replaceAll((key, value) -> {
          if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue(); // or use .toString() or .doubleValue() based on your requirement
          }
          return value;
        });

        float[] vector = null;
        if (queryParams.retrieveEmbeddings()) {

          Points.VectorsOutput vectors = currentPoint.getVectors();
          if (vectors != null && vectors.getSerializedSize() > 0) {
            vector = new float[vectors.getVector().getDataCount()];
            for (int i = 0; i < vectors.getVector().getDataCount(); i++) {
              vector[i] = (float) vectors.getVector().getData(i);
            }
          }
        }

        return new BaseStoreService.Row<>(id,
                                   vector != null ? new Embedding(vector) : null,
                                   new TextSegment(text, Metadata.from(metadataMap)));
      } catch (Exception e) {
        LOGGER.error("Error while fetching next row", e);
        throw new NoSuchElementException("Error processing next row");
      }
    }

    private void fetchNextPage() {
      try {
        Points.ScrollPoints.Builder request = Points.ScrollPoints.newBuilder()
            .setCollectionName(storeName)
            .setLimit((int) queryParams.pageSize());

        if (queryParams.retrieveEmbeddings()) {
          request.setWithVectors(Points.WithVectorsSelector.newBuilder().setEnable(true).build());
        }

        if (nextOffset != null) {
          request.setOffset(nextOffset);
        }

        Points.ScrollResponse response = client.scrollAsync(request.build()).get();
        List<Points.RetrievedPoint> points = response.getResultList();

        if (!points.isEmpty()) {
          this.pointIterator = points.iterator();
          this.nextOffset = response.getNextPageOffset();
          this.hasMorePages = nextOffset.hasNum() || nextOffset.hasUuid();
        } else {
          this.hasMorePages = false;
        }
      } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException("Error fetching Qdrant points", e);
      }
    }
  }
}

final class JsonFactory {
  public static String toJson(Map<String, JsonWithInt.Value> map)
      throws InvalidProtocolBufferException {

    Struct.Builder structBuilder = Struct.newBuilder();
    map.forEach((key, value) -> structBuilder.putFields(key, toProtobufValue(value)));
    return JsonFormat.printer().print(structBuilder.build());
  }

  private static Value toProtobufValue(io.qdrant.client.grpc.JsonWithInt.Value value) {
    switch (value.getKindCase()) {
      case NULL_VALUE:
        return Value.newBuilder().setNullValueValue(0).build();

      case BOOL_VALUE:
        return Value.newBuilder().setBoolValue(value.getBoolValue()).build();

      case STRING_VALUE:
        return Value.newBuilder().setStringValue(value.getStringValue()).build();

      case INTEGER_VALUE:
        return Value.newBuilder().setNumberValue(value.getIntegerValue()).build();

      case DOUBLE_VALUE:
        return Value.newBuilder().setNumberValue(value.getDoubleValue()).build();

      case STRUCT_VALUE:
        Struct.Builder structBuilder = Struct.newBuilder();
        value.getStructValue()
            .getFieldsMap()
            .forEach(
                (key, val) -> {
                  structBuilder.putFields(key, toProtobufValue(val));
                });
        return Value.newBuilder().setStructValue(structBuilder).build();

      case LIST_VALUE:
        Value.Builder listBuilder = Value.newBuilder();
        value.getListValue().getValuesList().stream()
            .map(JsonFactory::toProtobufValue)
            .forEach(listBuilder.getListValueBuilder()::addValues);
        return listBuilder.build();

      default:
        throw new IllegalArgumentException("Unsupported payload value type: " + value.getKindCase());
    }
  }
}
