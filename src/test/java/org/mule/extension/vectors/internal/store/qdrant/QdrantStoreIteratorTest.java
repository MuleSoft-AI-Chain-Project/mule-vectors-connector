package org.mule.extension.vectors.internal.store.qdrant;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Points;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.qdrant.QdrantStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.*;

import com.google.common.util.concurrent.Futures;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QdrantStoreIteratorTest {
    @Mock QdrantStoreConnection connection;
    @Mock QdrantClient client;
    @Mock QueryParameters queryParameters;

    @BeforeEach
    void setUp() {
        lenient().when(connection.getClient()).thenReturn(client);
        lenient().when(connection.getTextSegmentKey()).thenReturn("text");
        lenient().when(queryParameters.pageSize()).thenReturn(2);
        lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
    }

    @Test
    void next_returnsVectorStoreRowWithCorrectFields() throws Exception {
        Points.RetrievedPoint point = mock(Points.RetrievedPoint.class);
        Points.PointId pointId = Points.PointId.newBuilder().setUuid("id1").build();
        when(point.getId()).thenReturn(pointId);
        when(point.getPayloadOrDefault(eq("text"), any())).thenReturn(
                io.qdrant.client.grpc.JsonWithInt.Value.newBuilder().setStringValue("doc1").build()
        );
        when(point.getPayloadMap()).thenReturn(Collections.singletonMap("foo",
                io.qdrant.client.grpc.JsonWithInt.Value.newBuilder().setStringValue("bar").build()));
        Points.VectorOutput vectorOutput = Points.VectorOutput.newBuilder()
            .addData(0.1f)
            .addData(0.2f)
            .addData(0.3f)
            .build();
        Points.VectorsOutput vectorsOutput = Points.VectorsOutput.newBuilder().setVector(vectorOutput).build();
        when(point.getVectors()).thenReturn(vectorsOutput);

        Points.ScrollResponse response = mock(Points.ScrollResponse.class);
        when(response.getResultList()).thenReturn(List.of(point));
        when(response.getNextPageOffset()).thenReturn(Points.PointId.newBuilder().setUuid("id2").build());

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
        // Force fetchNextPage by calling hasNext()
        assertThatThrownBy(iterator::hasNext)
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Error fetching Qdrant points");
    }
}
