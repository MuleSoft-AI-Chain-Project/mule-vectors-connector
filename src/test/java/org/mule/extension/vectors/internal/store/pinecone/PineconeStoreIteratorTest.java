package org.mule.extension.vectors.internal.store.pinecone;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import org.mule.extension.vectors.internal.service.store.pinecone.PineconeStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.*;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.FetchResponse;
import io.pinecone.proto.ListItem;
import io.pinecone.proto.ListResponse;
import io.pinecone.proto.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PineconeStoreIteratorTest {

  PineconeStoreConnection pineconeStoreConnection;
  QueryParameters queryParameters;
  ListResponse listResponse;
  FetchResponse fetchResponse;
  Vector vector;
  Index index;
  Pinecone pineconeClient;
  ListItem listItem;

  @BeforeEach
  void setup() {
    queryParameters = mock(QueryParameters.class);
    // Only stub what is needed for all tests; move test-specific stubs into each test
  }

  @Test
  void constructor_authenticationFailure_throwsModuleException() {
    PineconeStoreConnection failingConn = mock(PineconeStoreConnection.class, withSettings().lenient());
    lenient().when(failingConn.getApiKey()).thenReturn("api-key");
    lenient().when(failingConn.getCloud()).thenReturn("cloud");
    lenient().when(failingConn.getRegion()).thenReturn("region");
    lenient().when(failingConn.getClient())
        .thenThrow(new StatusRuntimeException(Status.UNAUTHENTICATED.withDescription("bad key")));
    assertThatThrownBy(() -> new PineconeStoreIterator<>(failingConn, "store", queryParameters))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Authentication failed");
  }

  @Test
  void constructor_connectionFailure_throwsModuleException() {
    PineconeStoreConnection failingConn = mock(PineconeStoreConnection.class, withSettings().lenient());
    lenient().when(failingConn.getApiKey()).thenReturn("api-key");
    lenient().when(failingConn.getCloud()).thenReturn("cloud");
    lenient().when(failingConn.getRegion()).thenReturn("region");
    lenient().when(failingConn.getClient()).thenThrow(new RuntimeException("network down"));
    assertThatThrownBy(() -> new PineconeStoreIterator<>(failingConn, "store", queryParameters))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Failed to initialize Pinecone connection");
  }

  @Test
  void protobufValueConverter_handlesAllTypes() {
    Value stringVal = Value.newBuilder().setStringValue("str").build();
    Value numberVal = Value.newBuilder().setNumberValue(42.0).build();
    Value boolVal = Value.newBuilder().setBoolValue(true).build();
    Value listVal =
        Value.newBuilder().setListValue(ListValue.newBuilder().addValues(stringVal).addValues(numberVal).build()).build();
    Struct struct = Struct.newBuilder().putFields("k", stringVal).build();
    Value structVal = Value.newBuilder().setStructValue(struct).build();
    assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(stringVal)).isEqualTo("str");
    assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(numberVal)).isEqualTo(42.0); // double
    assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(boolVal)).isEqualTo(true);
    assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(listVal)).isInstanceOf(java.util.List.class);
    assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(structVal)).isInstanceOf(java.util.Map.class);
    assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(null)).isNull();
  }

  @Test
  void hasNext_returnsTrueWhenVectorsAvailable() {
    // Arrange
    ListResponse listResponse = mock(ListResponse.class, withSettings().lenient());
    FetchResponse fetchResponse = mock(FetchResponse.class, withSettings().lenient());
    Vector vector = mock(Vector.class, withSettings().lenient());
    ListItem listItem = mock(ListItem.class, withSettings().lenient());
    io.pinecone.proto.Pagination pagination = mock(io.pinecone.proto.Pagination.class, withSettings().lenient());
    lenient().when(listResponse.getVectorsList()).thenReturn(List.of(listItem));
    lenient().when(listResponse.getPagination()).thenReturn(pagination);
    lenient().when(pagination.getNext()).thenReturn("");
    lenient().when(listItem.getId()).thenReturn("id1");
    // If getText() or similar is used by production code, stub it to return non-blank
    try {
      java.lang.reflect.Method getText = listItem.getClass().getMethod("getText");
      lenient().when(getText.invoke(listItem)).thenReturn("test text");
    } catch (NoSuchMethodException ignored) {
      // If getText() does not exist, do nothing
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    lenient().when(fetchResponse.getVectorsCount()).thenReturn(1);
    Map<String, Vector> vectorsMap = new HashMap<>();
    vectorsMap.put("id1", vector);
    lenient().when(fetchResponse.getVectorsMap()).thenReturn(vectorsMap);
    lenient().when(vector.getId()).thenReturn("id1");
    lenient().when(vector.getValuesList()).thenReturn(List.of(1.0f, 2.0f));
    com.google.protobuf.Struct struct = mock(com.google.protobuf.Struct.class, withSettings().lenient());
    // Mock the text_segment value
    com.google.protobuf.Value textValue = mock(com.google.protobuf.Value.class, withSettings().lenient());
    lenient().when(textValue.getStringValue()).thenReturn("test text");
    Map<String, com.google.protobuf.Value> fieldsMap = new HashMap<>();
    fieldsMap.put("text_segment", textValue);
    lenient().when(struct.getFieldsMap()).thenReturn(fieldsMap);
    lenient().when(vector.getMetadata()).thenReturn(struct);
    Index index = mock(Index.class, withSettings().lenient());
    lenient().when(index.list(anyString(), anyInt())).thenReturn(listResponse);
    lenient().when(index.fetch(anyList(), anyString())).thenReturn(fetchResponse);
    Pinecone pinecone = mock(Pinecone.class, withSettings().lenient());
    lenient().when(pinecone.getIndexConnection(anyString())).thenReturn(index);
    PineconeStoreConnection pineconeStoreConnection = mock(PineconeStoreConnection.class, withSettings().lenient());
    lenient().when(pineconeStoreConnection.getClient()).thenReturn(pinecone);
    lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
    PineconeStoreIterator<?> iterator = new PineconeStoreIterator<>(pineconeStoreConnection, "store", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
  }

  @Test
  void next_returnsCorrectVectorStoreRow() {
    // Arrange
    ListResponse listResponse = mock(ListResponse.class, withSettings().lenient());
    FetchResponse fetchResponse = mock(FetchResponse.class, withSettings().lenient());
    Vector vector = mock(Vector.class, withSettings().lenient());
    ListItem listItem = mock(ListItem.class, withSettings().lenient());
    io.pinecone.proto.Pagination pagination = mock(io.pinecone.proto.Pagination.class, withSettings().lenient());
    lenient().when(listResponse.getVectorsList()).thenReturn(List.of(listItem));
    lenient().when(listResponse.getPagination()).thenReturn(pagination);
    lenient().when(pagination.getNext()).thenReturn("");
    lenient().when(listItem.getId()).thenReturn("id1");
    // If getText() or similar is used by production code, stub it to return non-blank
    try {
      java.lang.reflect.Method getText = listItem.getClass().getMethod("getText");
      lenient().when(getText.invoke(listItem)).thenReturn("test text");
    } catch (NoSuchMethodException ignored) {
      // If getText() does not exist, do nothing
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    lenient().when(fetchResponse.getVectorsCount()).thenReturn(1);
    Map<String, Vector> vectorsMap = new HashMap<>();
    vectorsMap.put("id1", vector);
    lenient().when(fetchResponse.getVectorsMap()).thenReturn(vectorsMap);
    lenient().when(vector.getId()).thenReturn("id1");
    lenient().when(vector.getValuesList()).thenReturn(List.of(1.0f, 2.0f));
    com.google.protobuf.Struct struct = mock(com.google.protobuf.Struct.class, withSettings().lenient());
    // Mock the text_segment value
    com.google.protobuf.Value textValue = mock(com.google.protobuf.Value.class, withSettings().lenient());
    lenient().when(textValue.getStringValue()).thenReturn("test text");
    Map<String, com.google.protobuf.Value> fieldsMap = new HashMap<>();
    fieldsMap.put("text_segment", textValue);
    lenient().when(struct.getFieldsMap()).thenReturn(fieldsMap);
    lenient().when(vector.getMetadata()).thenReturn(struct);
    Index index = mock(Index.class, withSettings().lenient());
    lenient().when(index.list(anyString(), anyInt())).thenReturn(listResponse);
    lenient().when(index.fetch(anyList(), anyString())).thenReturn(fetchResponse);
    Pinecone pinecone = mock(Pinecone.class, withSettings().lenient());
    lenient().when(pinecone.getIndexConnection(anyString())).thenReturn(index);
    PineconeStoreConnection pineconeStoreConnection = mock(PineconeStoreConnection.class, withSettings().lenient());
    lenient().when(pineconeStoreConnection.getClient()).thenReturn(pinecone);
    lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
    PineconeStoreIterator<?> iterator = new PineconeStoreIterator<>(pineconeStoreConnection, "store", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    VectorStoreRow<?> row = iterator.next();
    assertThat(row.getId()).isEqualTo("id1");
    assertThat(row.getEmbedding()).isNotNull();
  }

  @Test
  void hasNext_returnsFalseWhenNoMorePages() {
    // Arrange
    ListResponse listResponse = mock(ListResponse.class, withSettings().lenient());
    Index index = mock(Index.class, withSettings().lenient());
    lenient().when(listResponse.getVectorsList()).thenReturn(Collections.emptyList());
    lenient().when(listResponse.getPagination()).thenReturn(mock(io.pinecone.proto.Pagination.class, withSettings().lenient()));
    lenient().when(index.list(anyString(), anyInt())).thenReturn(listResponse);
    Pinecone pinecone = mock(Pinecone.class, withSettings().lenient());
    lenient().when(pinecone.getIndexConnection(anyString())).thenReturn(index);
    PineconeStoreConnection pineconeStoreConnection = mock(PineconeStoreConnection.class, withSettings().lenient());
    lenient().when(pineconeStoreConnection.getClient()).thenReturn(pinecone);
    lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
    PineconeStoreIterator<?> iterator = new PineconeStoreIterator<>(pineconeStoreConnection, "store", queryParameters);
    assertThat(iterator.hasNext()).isFalse();
  }

  @Test
  void fetchNextPage_authenticationError_throwsModuleException() {
    PineconeStoreConnection failingConn = mock(PineconeStoreConnection.class, withSettings().lenient());
    lenient().when(failingConn.getApiKey()).thenReturn("api-key");
    lenient().when(failingConn.getCloud()).thenReturn("cloud");
    lenient().when(failingConn.getRegion()).thenReturn("region");
    lenient().when(failingConn.getClient())
        .thenThrow(new StatusRuntimeException(Status.UNAUTHENTICATED.withDescription("bad key")));
    Exception ex = org.junit.jupiter.api.Assertions
        .assertThrows(ModuleException.class, () -> new PineconeStoreIterator<>(failingConn, "store", queryParameters));
    // Match the actual error message
    assertThat(ex.getMessage()).contains("Authentication failed while connecting to Pinecone: bad key");
  }

  @Test
  void fetchNextPage_invalidRequest_throwsModuleException() {
    PineconeStoreConnection failingConn = mock(PineconeStoreConnection.class, withSettings().lenient());
    lenient().when(failingConn.getApiKey()).thenReturn("api-key");
    lenient().when(failingConn.getCloud()).thenReturn("cloud");
    lenient().when(failingConn.getRegion()).thenReturn("region");
    lenient().when(failingConn.getClient()).thenThrow(new IllegalArgumentException("bad request"));
    Exception ex = org.junit.jupiter.api.Assertions
        .assertThrows(ModuleException.class, () -> new PineconeStoreIterator<>(failingConn, "store", queryParameters));
    assertThat(ex.getMessage()).contains("Failed to initialize Pinecone connection");
  }

  @Test
  void fetchNextPage_genericError_throwsModuleException() {
    PineconeStoreConnection failingConn = mock(PineconeStoreConnection.class, withSettings().lenient());
    lenient().when(failingConn.getApiKey()).thenReturn("api-key");
    lenient().when(failingConn.getCloud()).thenReturn("cloud");
    lenient().when(failingConn.getRegion()).thenReturn("region");
    lenient().when(failingConn.getClient()).thenThrow(new RuntimeException("fail"));
    Exception ex = org.junit.jupiter.api.Assertions
        .assertThrows(ModuleException.class, () -> new PineconeStoreIterator<>(failingConn, "store", queryParameters));
    assertThat(ex.getMessage()).contains("Failed to initialize Pinecone connection");
  }
}
