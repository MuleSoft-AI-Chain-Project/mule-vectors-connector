package org.mule.extension.vectors.internal.store.pinecone;

import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;
import org.mule.extension.vectors.internal.store.VectorStoreRow;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.FetchResponse;
import io.pinecone.proto.ListResponse;
import io.pinecone.proto.Vector;
import io.grpc.StatusRuntimeException;

import java.util.*;
import java.util.stream.Collectors;

public class PineconeStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PineconeStoreIterator.class);

  private final String apiKey;
  private final String storeName;
  private final String cloud;
  private final String region;
  private final QueryParameters queryParams;

  private final Pinecone client;
  private final Index index;
  private final PineconeStoreConnection pineconeStoreConnection;
  private Iterator<io.pinecone.proto.Vector> vectorIterator = Collections.emptyIterator();
  private boolean hasMorePages = true;
  private String paginationToken = null;

  public PineconeStoreIterator(
      PineconeStoreConnection pineconeStoreConnection,
      String storeName,
      QueryParameters queryParams
  ) {
    this.apiKey = pineconeStoreConnection.getApiKey();
    this.storeName = storeName;
    this.cloud = pineconeStoreConnection.getCloud();
    this.region = pineconeStoreConnection.getRegion();
    this.queryParams = queryParams;
    try {
      this.pineconeStoreConnection = pineconeStoreConnection;
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
          .filter(e -> !"text_segment".equals(e.getKey()))
          .collect(Collectors.toMap(Map.Entry::getKey, e -> ProtobufValueConverter.convertProtobufValue(e.getValue())));

      if (entry.getMetadata().getFieldsMap().containsKey("text_segment")) {
        text = entry.getMetadata().getFieldsMap().get("text_segment").getStringValue();
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
      LOGGER.error("Error while fetching next row", e);
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
      LOGGER.error("Authentication error fetching Pinecone points", e);
      throw new ModuleException("Authentication failed: " + e.getStatus().getDescription(), MuleVectorsErrorType.AUTHENTICATION, e);
    } catch (IllegalArgumentException e) {
      LOGGER.error("Invalid request fetching Pinecone points", e);
      throw new ModuleException("Invalid request to Pinecone: " + e.getMessage(), MuleVectorsErrorType.INVALID_REQUEST, e);
    } catch (Exception e) {
      LOGGER.error("Error fetching Pinecone points", e);
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
          .collect(Collectors.toList());

      paginationToken = listResponse.getPagination().getNext();
      hasMorePages = paginationToken != null && !paginationToken.isEmpty();

      return ids;

    } catch (StatusRuntimeException e) {
      LOGGER.error("Authentication error retrieving batch of IDs", e);
      throw new ModuleException("Authentication failed: " + e.getStatus().getDescription(), MuleVectorsErrorType.AUTHENTICATION, e);
    } catch (IllegalArgumentException e) {
      LOGGER.error("Invalid request retrieving batch of IDs", e);
      throw new ModuleException("Invalid request to Pinecone: " + e.getMessage(), MuleVectorsErrorType.INVALID_REQUEST, e);
    } catch (Exception e) {
      LOGGER.error("Error retrieving next batch of IDs", e);
      throw new ModuleException("Error fetching from Pinecone", MuleVectorsErrorType.STORE_SERVICES_FAILURE, e);
    }
  }

  public static class ProtobufValueConverter {
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
            .collect(Collectors.toList());
      }
      if (value.hasStructValue()) return value.getStructValue().getFieldsMap();
      return null;
    }
  }
}
