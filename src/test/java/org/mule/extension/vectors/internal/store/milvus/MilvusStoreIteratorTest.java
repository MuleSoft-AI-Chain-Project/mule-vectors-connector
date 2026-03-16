package org.mule.extension.vectors.internal.store.milvus;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.milvus.MilvusStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.milvus.MilvusStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.*;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.milvus.client.MilvusServiceClient;
import io.milvus.exception.MilvusException;
import io.milvus.orm.iterator.QueryIterator;
import io.milvus.param.R;
import io.milvus.param.dml.QueryIteratorParam;
import io.milvus.response.QueryResultsWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MilvusStoreIteratorTest {

  @Mock
  MilvusStoreConnection connection;
  @Mock
  MilvusServiceClient milvusClient;
  @Mock
  QueryParameters queryParameters;
  @Mock
  QueryIterator queryIterator;

  @BeforeEach
  void setUp() {
    lenient().when(connection.getClient()).thenReturn(milvusClient);
    lenient().when(connection.getIdFieldName()).thenReturn("id");
    lenient().when(connection.getTextFieldName()).thenReturn("text");
    lenient().when(connection.getMetadataFieldName()).thenReturn("metadata");
    lenient().when(connection.getVectorFieldName()).thenReturn("vector");
    lenient().when(queryParameters.pageSize()).thenReturn(10);
    lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
  }

  @SuppressWarnings("unchecked")
  private R<QueryIterator> successResult() {
    R<QueryIterator> result = mock(R.class);
    lenient().when(result.getStatus()).thenReturn(R.Status.Success.getCode());
    lenient().when(result.getData()).thenReturn(queryIterator);
    return result;
  }

  @SuppressWarnings("unchecked")
  private R<QueryIterator> failedResult(int status, String message) {
    R<QueryIterator> result = mock(R.class);
    lenient().when(result.getStatus()).thenReturn(status);
    lenient().when(result.getMessage()).thenReturn(message);
    return result;
  }

  @SuppressWarnings("unchecked")
  private QueryResultsWrapper.RowRecord mockRow(String id, String text, String metadata, List<Float> vector) {
    QueryResultsWrapper.RowRecord record = mock(QueryResultsWrapper.RowRecord.class);
    Map<String, Object> fieldValues = new HashMap<>();
    fieldValues.put("id", id);
    fieldValues.put("text", text);
    fieldValues.put("metadata", metadata);
    if (vector != null) {
      fieldValues.put("vector", vector);
    }
    lenient().when(record.getFieldValues()).thenReturn(fieldValues);
    return record;
  }

  private void setupSuccessIterator() {
    R<QueryIterator> result = successResult();
    when(milvusClient.queryIterator(any(QueryIteratorParam.class))).thenReturn(result);
  }

  @Test
  void next_returnsRowWithEmbedding() {
    QueryResultsWrapper.RowRecord row = mockRow("id1", "hello", "{\"foo\":\"bar\"}", List.of(0.1f, 0.2f, 0.3f));
    when(queryIterator.next()).thenReturn(List.of(row)).thenReturn(Collections.emptyList());
    setupSuccessIterator();

    MilvusStoreIterator<TextSegment> iterator = new MilvusStoreIterator<>(connection, "store", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    var result = iterator.next();
    assertThat(result.getId()).isEqualTo("id1");
    assertThat(result.getEmbedding().vector()).containsExactly(0.1f, 0.2f, 0.3f);
    assertThat(((TextSegment) result.getEmbedded()).text()).isEqualTo("hello");
  }

  @Test
  void next_withoutEmbeddings_returnsNullEmbedding() {
    when(queryParameters.retrieveEmbeddings()).thenReturn(false);
    QueryResultsWrapper.RowRecord row = mockRow("id1", "hello", "{\"foo\":\"bar\"}", null);
    when(queryIterator.next()).thenReturn(List.of(row)).thenReturn(Collections.emptyList());
    setupSuccessIterator();

    MilvusStoreIterator<TextSegment> iterator = new MilvusStoreIterator<>(connection, "store", queryParameters);
    var result = iterator.next();
    assertThat(result.getEmbedding()).isNull();
  }

  @Test
  void next_whenNoMoreElements_throwsNoSuchElement() {
    when(queryIterator.next()).thenReturn(Collections.emptyList());
    setupSuccessIterator();

    MilvusStoreIterator<TextSegment> iterator = new MilvusStoreIterator<>(connection, "store", queryParameters);
    assertThat(iterator.hasNext()).isFalse();
    assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void constructor_failedQueryIterator_throwsMilvusException() {
    R<QueryIterator> failed = failedResult(1, "Connection refused");
    when(milvusClient.queryIterator(any(QueryIteratorParam.class))).thenReturn(failed);

    assertThatThrownBy(() -> new MilvusStoreIterator<>(connection, "store", queryParameters))
        .isInstanceOf(MilvusException.class);
  }

  @Test
  void fetchNextBatch_unauthenticated_throwsModuleException() {
    QueryResultsWrapper.RowRecord row = mockRow("id1", "hello", "{}", List.of(0.1f));
    when(queryIterator.next())
        .thenReturn(List.of(row))
        .thenThrow(new StatusRuntimeException(Status.UNAUTHENTICATED.withDescription("bad token")));
    setupSuccessIterator();

    MilvusStoreIterator<TextSegment> iterator = new MilvusStoreIterator<>(connection, "store", queryParameters);
    iterator.next();
    assertThatThrownBy(iterator::hasNext)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Authentication failed");
  }

  @Test
  void fetchNextBatch_invalidArgument_throwsModuleException() {
    QueryResultsWrapper.RowRecord row = mockRow("id1", "hello", "{}", List.of(0.1f));
    when(queryIterator.next())
        .thenReturn(List.of(row))
        .thenThrow(new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("bad arg")));
    setupSuccessIterator();

    MilvusStoreIterator<TextSegment> iterator = new MilvusStoreIterator<>(connection, "store", queryParameters);
    iterator.next();
    assertThatThrownBy(iterator::hasNext)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Invalid request to Milvus");
  }

  @Test
  void fetchNextBatch_otherGrpcError_throwsServiceError() {
    QueryResultsWrapper.RowRecord row = mockRow("id1", "hello", "{}", List.of(0.1f));
    when(queryIterator.next())
        .thenReturn(List.of(row))
        .thenThrow(new StatusRuntimeException(Status.INTERNAL.withDescription("internal error")));
    setupSuccessIterator();

    MilvusStoreIterator<TextSegment> iterator = new MilvusStoreIterator<>(connection, "store", queryParameters);
    iterator.next();
    assertThatThrownBy(iterator::hasNext)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Milvus service error");
  }

  @Test
  void fetchNextBatch_milvusException_throwsModuleException() {
    QueryResultsWrapper.RowRecord row = mockRow("id1", "hello", "{}", List.of(0.1f));
    when(queryIterator.next())
        .thenReturn(List.of(row))
        .thenThrow(new MilvusException("milvus error", 500));
    setupSuccessIterator();

    MilvusStoreIterator<TextSegment> iterator = new MilvusStoreIterator<>(connection, "store", queryParameters);
    iterator.next();
    assertThatThrownBy(iterator::hasNext)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Milvus error");
  }

  @Test
  void pagination_multiplePages() {
    QueryResultsWrapper.RowRecord row1 = mockRow("id1", "hello1", "{}", List.of(0.1f));
    QueryResultsWrapper.RowRecord row2 = mockRow("id2", "hello2", "{}", List.of(0.2f));
    when(queryIterator.next())
        .thenReturn(List.of(row1))
        .thenReturn(List.of(row2))
        .thenReturn(Collections.emptyList());
    setupSuccessIterator();

    MilvusStoreIterator<TextSegment> iterator = new MilvusStoreIterator<>(connection, "store", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next().getId()).isEqualTo("id1");
    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next().getId()).isEqualTo("id2");
    assertThat(iterator.hasNext()).isFalse();
  }
}
