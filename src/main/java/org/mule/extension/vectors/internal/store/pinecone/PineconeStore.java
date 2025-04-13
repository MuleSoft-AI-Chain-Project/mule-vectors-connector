package org.mule.extension.vectors.internal.store.pinecone;

import com.google.protobuf.Value;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.ValidationUtils;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.FetchResponse;
import io.pinecone.proto.ListResponse;
import io.pinecone.proto.Vector;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.BaseStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PineconeStore extends BaseStore {

  protected static final Logger LOGGER = LoggerFactory.getLogger(PineconeStore.class);

  private String apiKey;
  private String cloud;
  private String region;

  public PineconeStore(StoreConfiguration storeConfiguration, PineconeStoreConnection pineconeStoreConnection, String storeName, QueryParameters queryParams, int dimension, boolean createStore) {

    super(storeConfiguration, pineconeStoreConnection, storeName, queryParams, dimension, createStore);

    if(queryParams != null) ValidationUtils.ensureBetween(queryParams.pageSize(), 0, 100, "pageSize");
    this.apiKey = pineconeStoreConnection.getApiKey();
    this.cloud = pineconeStoreConnection.getCloud();
    this.region = pineconeStoreConnection.getRegion();
  }

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
          .build():

        PineconeEmbeddingStore.builder()
            .apiKey(apiKey)
            .index(storeName)
            .nameSpace("ns0mc_" + storeName)
            .build();
  }

  @Override
  public PineconeStore.RowIterator rowIterator() {
    try {
      return new PineconeStore.RowIterator();
    } catch (Exception e) {
      LOGGER.error("Error while creating row iterator", e);
      throw new RuntimeException(e);
    }
  }

  public class RowIterator extends BaseStore.RowIterator {

    private final Pinecone client;
    private final Index index;
    private Iterator<io.pinecone.proto.Vector> vectorIterator = Collections.emptyIterator();
    private boolean hasMorePages = true;
    private String paginationToken = null;

    public RowIterator() throws Exception {
      this.client = new Pinecone.Builder(apiKey).build();
      this.index = client.getIndexConnection(storeName);
      fetchNextPage(); // Load first batch
    }

    @Override
    public boolean hasNext() {
      if (!vectorIterator.hasNext() && hasMorePages) {
        fetchNextPage();
      }
      return vectorIterator.hasNext();
    }

    @Override
    public BaseStore.Row<?> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      try {
        Vector entry = vectorIterator.next();
        String id = entry.getId();
        String text = "";

        Map<String, Object> metadataMap = entry.getMetadata().getFieldsMap().entrySet().stream()
            .filter(e -> !"text_segment".equals(e.getKey())) // Exclude key "text_segment"
            .collect(Collectors.toMap(Map.Entry::getKey, e -> ProtobufValueConverter.convertProtobufValue(e.getValue())));

        text = entry.getMetadata().getFieldsMap().get("text_segment").getStringValue();

        float[] vector = null;

        if(queryParams.retrieveEmbeddings()) {
          List<Float> floatList = entry.getValuesList();  // Assuming this is your List<Float>
          vector = new float[floatList.size()];
          for (int i = 0; i < floatList.size(); i++) {
            vector[i] = floatList.get(i);  // Unbox each Float to primitive float
          }
        }

        return new BaseStore.Row<>(
            id,
            vector != null ? new Embedding(vector) : null,
            new TextSegment(text, Metadata.from(metadataMap))
        );

      } catch (Exception e) {
        LOGGER.error("Error while fetching next row", e);
        throw new NoSuchElementException("Error processing next row: " + e.getMessage());
      }
    }

    private void fetchNextPage() {
      try {
        if (!hasMorePages) return;

        List<String> idBatch = getNextBatchOfIds(queryParams.pageSize());
        if (idBatch.isEmpty()) {
          hasMorePages = false;
          return;
        }

        FetchResponse response = index.fetch(idBatch, "ns0mc_" + storeName);
        if (response.getVectorsCount() > 0) {
          vectorIterator = response.getVectorsMap().values().iterator();
        } else {
          hasMorePages = false;
        }

      } catch (Exception e) {
        LOGGER.error("Error fetching Pinecone points", e);
        throw new RuntimeException("Error fetching Pinecone points", e);
      }
    }

    private List<String> getNextBatchOfIds(int limit) {
      try {
        ListResponse listResponse = paginationToken == null
            ? index.list("ns0mc_" + storeName, limit)
            : index.list(paginationToken, limit);

        List<String> ids = listResponse.getVectorsList().stream()
            .map(vector -> vector.getId())
            .collect(Collectors.toList());

        paginationToken = listResponse.getPagination().getNext();
        hasMorePages = paginationToken != null && !paginationToken.isEmpty();

        return ids;

      } catch (Exception e) {
        LOGGER.error("Error retrieving next batch of IDs", e);
        return Collections.emptyList();
      }
    }
  }

  public static class ProtobufValueConverter {
    public static Object convertProtobufValue(Value value) {
      if (value == null) return null;
      if (value.hasStringValue()) return value.getStringValue();
      if (value.hasNumberValue()) {
        double num = value.getNumberValue();
        return num == (long) num ? (long) num : num;
      }
      if (value.hasBoolValue()) return value.getBoolValue();
      if (value.hasListValue()) {
        return value.getListValue().getValuesList().stream()
            .map(ProtobufValueConverter::convertProtobufValue)
            .collect(Collectors.toList());
      }
      if (value.hasStructValue()) return value.getStructValue().getFieldsMap();
      return null;
    }
  }
}
