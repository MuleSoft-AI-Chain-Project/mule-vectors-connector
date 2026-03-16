package org.mule.extension.vectors.internal.store.qdrant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.qdrant.QdrantStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.*;

import com.google.common.util.concurrent.Futures;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Common;
import io.qdrant.client.grpc.Points;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QdrantStoreIteratorTest {

  @Mock
  QdrantStoreConnection connection;
  @Mock
  QdrantClient client;
  @Mock
  QueryParameters queryParameters;

  @BeforeEach
  void setUp() {
    lenient().when(connection.getClient()).thenReturn(client);
    lenient().when(connection.getTextSegmentKey()).thenReturn("text");
    lenient().when(queryParameters.pageSize()).thenReturn(2);
    lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
  }

  private Points.RetrievedPoint buildPoint(String id, String text, Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload,
                                           Points.VectorsOutput vectors) {
    Points.RetrievedPoint point = mock(Points.RetrievedPoint.class);
    Common.PointId pointId = Common.PointId.newBuilder().setUuid(id).build();
    when(point.getId()).thenReturn(pointId);
    when(point.getPayloadOrDefault(eq("text"), any())).thenReturn(
                                                                  io.qdrant.client.grpc.JsonWithInt.Value.newBuilder()
                                                                      .setStringValue(text).build());
    when(point.getPayloadMap()).thenReturn(payload);
    if (vectors != null) {
      when(point.getVectors()).thenReturn(vectors);
    }
    return point;
  }

  @Test
  void next_returnsVectorStoreRowWithLegacyDataList() throws Exception {
    Points.VectorOutput vectorOutput = Points.VectorOutput.newBuilder()
        .addData(0.1f).addData(0.2f).addData(0.3f).build();
    Points.VectorsOutput vectorsOutput = Points.VectorsOutput.newBuilder().setVector(vectorOutput).build();

    Points.RetrievedPoint point = buildPoint("id1", "doc1",
                                             Collections.singletonMap("foo", io.qdrant.client.grpc.JsonWithInt.Value.newBuilder()
                                                 .setStringValue("bar").build()),
                                             vectorsOutput);

    Points.ScrollResponse response = mock(Points.ScrollResponse.class);
    when(response.getResultList()).thenReturn(List.of(point));
    when(response.getNextPageOffset()).thenReturn(Common.PointId.newBuilder().setUuid("id2").build());
    when(client.scrollAsync(any())).thenReturn(Futures.immediateFuture(response));

    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    var row = iterator.next();
    assertThat(row.getId()).isEqualTo("id1");
    assertThat(row.getEmbedded()).isInstanceOf(TextSegment.class);
    assertThat(((TextSegment) row.getEmbedded()).text()).isEqualTo("doc1");
    assertThat(row.getEmbedding()).isInstanceOf(Embedding.class);
    assertThat(((Embedding) row.getEmbedding()).vector()).containsExactly(0.1f, 0.2f, 0.3f);
  }

  @Test
  void next_returnsVectorUsingDenseDataList() throws Exception {
    Points.DenseVector denseVector = Points.DenseVector.newBuilder()
        .addData(0.4f).addData(0.5f).addData(0.6f).build();
    Points.VectorOutput vectorOutput = Points.VectorOutput.newBuilder()
        .setDense(denseVector).build();
    Points.VectorsOutput vectorsOutput = Points.VectorsOutput.newBuilder().setVector(vectorOutput).build();

    Points.RetrievedPoint point = buildPoint("id2", "doc2",
                                             Collections.singletonMap("key", io.qdrant.client.grpc.JsonWithInt.Value.newBuilder()
                                                 .setStringValue("val").build()),
                                             vectorsOutput);

    Points.ScrollResponse response = mock(Points.ScrollResponse.class);
    when(response.getResultList()).thenReturn(List.of(point));
    when(response.getNextPageOffset()).thenReturn(Common.PointId.getDefaultInstance());
    when(client.scrollAsync(any())).thenReturn(Futures.immediateFuture(response));

    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    var row = iterator.next();
    assertThat(row.getEmbedding().vector()).containsExactly(0.4f, 0.5f, 0.6f);
  }

  @Test
  void next_withoutEmbeddings_returnsNullEmbedding() throws Exception {
    when(queryParameters.retrieveEmbeddings()).thenReturn(false);

    Points.RetrievedPoint point = buildPoint("id3", "doc3",
                                             Collections.singletonMap("m", io.qdrant.client.grpc.JsonWithInt.Value.newBuilder()
                                                 .setStringValue("v").build()),
                                             null);

    Points.ScrollResponse response = mock(Points.ScrollResponse.class);
    when(response.getResultList()).thenReturn(List.of(point));
    when(response.getNextPageOffset()).thenReturn(Common.PointId.getDefaultInstance());
    when(client.scrollAsync(any())).thenReturn(Futures.immediateFuture(response));

    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    var row = iterator.next();
    assertThat(row.getEmbedding()).isNull();
    assertThat(row.getEmbedded()).isInstanceOf(TextSegment.class);
  }

  @Test
  void next_handlesMetadataWithBigDecimalValues() throws Exception {
    Points.VectorOutput vectorOutput = Points.VectorOutput.newBuilder().addData(1.0f).build();
    Points.VectorsOutput vectorsOutput = Points.VectorsOutput.newBuilder().setVector(vectorOutput).build();

    Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload = new HashMap<>();
    payload.put("count", io.qdrant.client.grpc.JsonWithInt.Value.newBuilder().setDoubleValue(42.0).build());
    payload.put("intVal", io.qdrant.client.grpc.JsonWithInt.Value.newBuilder().setIntegerValue(99).build());

    Points.RetrievedPoint point = buildPoint("id4", "doc4", payload, vectorsOutput);

    Points.ScrollResponse response = mock(Points.ScrollResponse.class);
    when(response.getResultList()).thenReturn(List.of(point));
    when(response.getNextPageOffset()).thenReturn(Common.PointId.getDefaultInstance());
    when(client.scrollAsync(any())).thenReturn(Futures.immediateFuture(response));

    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    var row = iterator.next();
    assertThat(row.getId()).isEqualTo("id4");
  }

  @Test
  void next_whenNoMoreElements_throwsNoSuchElementException() throws Exception {
    Points.ScrollResponse response = mock(Points.ScrollResponse.class);
    when(response.getResultList()).thenReturn(Collections.emptyList());
    when(client.scrollAsync(any())).thenReturn(Futures.immediateFuture(response));
    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    assertThat(iterator.hasNext()).isFalse();
    assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void fetchNextPage_whenExecutionException_throwsModuleException() throws Exception {
    when(client.scrollAsync(any())).thenReturn(Futures.immediateFailedFuture(new RuntimeException("fail")));
    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    assertThatThrownBy(iterator::hasNext)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Error fetching Qdrant points");
  }

  @Test
  void fetchNextPage_whenUnauthenticated_throwsInvalidConnection() throws Exception {
    StatusRuntimeException sre = new StatusRuntimeException(Status.UNAUTHENTICATED.withDescription("bad token"));
    when(client.scrollAsync(any())).thenReturn(Futures.immediateFailedFuture(sre));
    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    assertThatThrownBy(iterator::hasNext)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Authentication failed");
  }

  @Test
  void fetchNextPage_whenInvalidArgument_throwsInvalidRequest() throws Exception {
    StatusRuntimeException sre = new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("bad arg"));
    when(client.scrollAsync(any())).thenReturn(Futures.immediateFailedFuture(sre));
    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    assertThatThrownBy(iterator::hasNext)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Invalid request to Qdrant");
  }

  @Test
  void fetchNextPage_whenOtherGrpcError_throwsServiceError() throws Exception {
    StatusRuntimeException sre = new StatusRuntimeException(Status.INTERNAL.withDescription("internal error"));
    when(client.scrollAsync(any())).thenReturn(Futures.immediateFailedFuture(sre));
    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    assertThatThrownBy(iterator::hasNext)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Qdrant service error");
  }

  @Test
  void fetchNextPage_whenInterrupted_throwsModuleException() throws Exception {
    when(client.scrollAsync(any())).thenAnswer(invocation -> {
      Thread.currentThread().interrupt();
      return Futures.immediateFailedFuture(new InterruptedException("interrupted"));
    });
    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    assertThatThrownBy(iterator::hasNext)
        .isInstanceOf(ModuleException.class);
    Thread.interrupted();
  }

  @Test
  void pagination_stopsWhenNextOffsetIsDefault() throws Exception {
    Points.VectorOutput vo = Points.VectorOutput.newBuilder().addData(1.0f).build();
    Points.VectorsOutput vso = Points.VectorsOutput.newBuilder().setVector(vo).build();
    Points.RetrievedPoint point = buildPoint("p1", "text1",
                                             Collections.singletonMap("k", io.qdrant.client.grpc.JsonWithInt.Value.newBuilder()
                                                 .setStringValue("v").build()),
                                             vso);

    Points.ScrollResponse response = mock(Points.ScrollResponse.class);
    when(response.getResultList()).thenReturn(List.of(point));
    when(response.getNextPageOffset()).thenReturn(Common.PointId.getDefaultInstance());
    when(client.scrollAsync(any())).thenReturn(Futures.immediateFuture(response));

    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    iterator.next();
    assertThat(iterator.hasNext()).isFalse();
    verify(client, times(1)).scrollAsync(any());
  }

  @Test
  void pagination_continuesWhenNextOffsetHasUuid() throws Exception {
    Points.VectorOutput vo = Points.VectorOutput.newBuilder().addData(1.0f).build();
    Points.VectorsOutput vso = Points.VectorsOutput.newBuilder().setVector(vo).build();

    Points.RetrievedPoint point1 = buildPoint("p1", "text1",
                                              Collections.singletonMap("k", io.qdrant.client.grpc.JsonWithInt.Value.newBuilder()
                                                  .setStringValue("v").build()),
                                              vso);
    Points.RetrievedPoint point2 = buildPoint("p2", "text2",
                                              Collections.singletonMap("k", io.qdrant.client.grpc.JsonWithInt.Value.newBuilder()
                                                  .setStringValue("v").build()),
                                              vso);

    Points.ScrollResponse firstResponse = mock(Points.ScrollResponse.class);
    when(firstResponse.getResultList()).thenReturn(List.of(point1));
    when(firstResponse.getNextPageOffset()).thenReturn(Common.PointId.newBuilder().setUuid("next-id").build());

    Points.ScrollResponse secondResponse = mock(Points.ScrollResponse.class);
    when(secondResponse.getResultList()).thenReturn(List.of(point2));
    when(secondResponse.getNextPageOffset()).thenReturn(Common.PointId.getDefaultInstance());

    when(client.scrollAsync(any()))
        .thenReturn(Futures.immediateFuture(firstResponse))
        .thenReturn(Futures.immediateFuture(secondResponse));

    QdrantStoreIterator<TextSegment> iterator = new QdrantStoreIterator<>(connection, "testStore", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next().getId()).isEqualTo("p1");
    assertThat(iterator.hasNext()).isTrue();
    assertThat(iterator.next().getId()).isEqualTo("p2");
    assertThat(iterator.hasNext()).isFalse();
  }
}
