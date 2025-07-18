package org.mule.extension.vectors.internal.service.store.pinecone;

import org.mule.extension.vectors.internal.connection.provider.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.runtime.extension.api.exception.ModuleException;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.FetchResponse;
import io.pinecone.proto.ListResponse;
import io.pinecone.proto.Vector;
import io.grpc.StatusRuntimeException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class PineconeStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private String TEXT_SEGMENT_MAP_KEY = "text_segment";

  private final String storeName;
  private final QueryParameters queryParams;

  private final Pinecone client;
  private final Index index;
  private Iterator<io.pinecone.proto.Vector> vectorIterator = Collections.emptyIterator();
  private boolean hasMorePages = true;
  private String paginationToken = null;

  public PineconeStoreIterator(
      PineconeStoreConnection pineconeStoreConnection,
      String storeName,
      QueryParameters queryParams
  ) {
    this.storeName = storeName;
    this.queryParams = queryParams;
    try {
      this.client =pineconeStoreConnection.getClient();
      this.index = client.getIndexConnection(storeName);
      fetchNextPage(); // Load first batch
    } catch (StatusRuntimeException e) {
      throw new ModuleException("Authentication failed while connecting to Pinecone: " + e.getStatus().getDescription(), MuleVectorsErrorType.AUTHENTICATION, e);
    } catch (Exception e) {
      throw new ModuleException("Failed to initialize Pinecone connection: " + e.getMessage(), MuleVectorsErrorType.CONNECTION_FAILED, e);
    }
  }

  @Override
  public boolean hasNext() {
    if (!vectorIterator.hasNext() && hasMorePages) {
      fetchNextPage();
    }
    return vectorIterator.hasNext();
  }

  @Override
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    try {
      Vector entry = vectorIterator.next();
      String id = entry.getId();
      String text = "";

      Map<String, Object> metadataMap = entry.getMetadata().getFieldsMap().entrySet().stream()
          .filter(e -> !TEXT_SEGMENT_MAP_KEY.equals(e.getKey()))
          .toList()
          .stream()
          .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> ProtobufValueConverter.convertProtobufValue(e.getValue())));

      if (entry.getMetadata().getFieldsMap().containsKey(TEXT_SEGMENT_MAP_KEY)) {
        text = entry.getMetadata().getFieldsMap().get(TEXT_SEGMENT_MAP_KEY).getStringValue();
      }

      float[] vector = null;

      if (queryParams.retrieveEmbeddings()) {
        List<Float> floatList = entry.getValuesList();
        vector = new float[floatList.size()];
        for (int i = 0; i < floatList.size(); i++) {
          vector[i] = floatList.get(i);
        }
      }

      // This is the only place you may want to adapt for Embedded type.
      // If you want to keep it generic, you can cast or use a factory.
      // For now, we keep it as TextSegment to match the original.
      @SuppressWarnings("unchecked")
      Embedded embedded = (Embedded) new TextSegment(text, Metadata.from(metadataMap));

      return new VectorStoreRow<>(
          id,
          vector != null ? new Embedding(vector) : null,
          embedded
      );

    } catch (NoSuchElementException e) {
      throw e;
    } catch (Exception e) {
      throw new ModuleException("Error processing next row: " + e.getMessage(), MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  private void fetchNextPage() {
    try {
      if (!hasMorePages) return;

      List<String> idBatch = getNextBatchOfIds((int) queryParams.pageSize());
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

    } catch (StatusRuntimeException e) {
      throw new ModuleException("Authentication failed: " + e.getStatus().getDescription(), MuleVectorsErrorType.AUTHENTICATION, e);
    } catch (IllegalArgumentException e) {
      throw new ModuleException("Invalid request to Pinecone: " + e.getMessage(), MuleVectorsErrorType.INVALID_REQUEST, e);
    } catch (Exception e) {
      throw new ModuleException("Error fetching from Pinecone", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  private List<String> getNextBatchOfIds(int limit) {
    try {
      ListResponse listResponse = paginationToken == null
          ? index.list("ns0mc_" + storeName, limit)
          : index.list(paginationToken, limit);

      List<String> ids = listResponse.getVectorsList().stream()
          .map(vector -> vector.getId())
          .toList();

      paginationToken = listResponse.getPagination().getNext();
      hasMorePages = paginationToken != null && !paginationToken.isEmpty();

      return ids;

    } catch (StatusRuntimeException e) {
      throw new ModuleException("Authentication failed: " + e.getStatus().getDescription(), MuleVectorsErrorType.AUTHENTICATION, e);
    } catch (IllegalArgumentException e) {
      throw new ModuleException("Invalid request to Pinecone: " + e.getMessage(), MuleVectorsErrorType.INVALID_REQUEST, e);
    } catch (Exception e) {
      throw new ModuleException("Error fetching from Pinecone", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  public static class ProtobufValueConverter {
    private ProtobufValueConverter() {}
    public static Object convertProtobufValue(com.google.protobuf.Value value) {
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
            .toList();
      }
      if (value.hasStructValue()) return value.getStructValue().getFieldsMap();
      return null;
    }
  }
}
