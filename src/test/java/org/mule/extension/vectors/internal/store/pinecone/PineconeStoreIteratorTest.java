package org.mule.extension.vectors.internal.store.pinecone;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.protobuf.ListValue;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.FetchResponse;
import io.pinecone.proto.ListResponse;
import io.pinecone.proto.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.store.VectorStoreRow;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.junit.jupiter.api.Disabled;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PineconeStoreIteratorTest {
    @Mock PineconeStoreConnection pineconeStoreConnection;
    @Mock QueryParameters queryParameters;
    @Mock Pinecone pineconeClient;
    @Mock Index index;

    @BeforeEach
    void setup() {
        lenient().when(pineconeStoreConnection.getApiKey()).thenReturn("api-key");
        lenient().when(pineconeStoreConnection.getCloud()).thenReturn("cloud");
        lenient().when(pineconeStoreConnection.getRegion()).thenReturn("region");
        lenient().when(pineconeStoreConnection.getClient()).thenReturn(pineconeClient);
        lenient().when(pineconeClient.getIndexConnection(anyString())).thenReturn(index);
        lenient().when(queryParameters.pageSize()).thenReturn(2);
        lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
    }

    @Test
    @Disabled("Disabled due to missing Pagination type in Pinecone proto; cannot mock pagination-dependent logic.")
    void constructor_success_doesNotThrow() {
        // Test disabled: Pagination type is unavailable, so we cannot mock it. Pagination logic is not tested here.
    }

    @Test
    void constructor_authenticationFailure_throwsModuleException() {
        when(pineconeStoreConnection.getClient()).thenThrow(new StatusRuntimeException(Status.UNAUTHENTICATED.withDescription("bad key")));
        try {
            new PineconeStoreIterator<>(pineconeStoreConnection, "store", queryParameters);
            fail("Expected ModuleException");
        } catch (ModuleException e) {
            assertThat(e.getMessage()).contains("Authentication failed");
        }
    }

    @Test
    void constructor_connectionFailure_throwsModuleException() {
        when(pineconeStoreConnection.getClient()).thenThrow(new RuntimeException("network down"));
        try {
            new PineconeStoreIterator<>(pineconeStoreConnection, "store", queryParameters);
            fail("Expected ModuleException");
        } catch (ModuleException e) {
            assertThat(e.getMessage()).contains("Failed to initialize Pinecone connection");
        }
    }

    @Test
    void protobufValueConverter_handlesAllTypes() {
        Value stringVal = Value.newBuilder().setStringValue("str").build();
        Value numberVal = Value.newBuilder().setNumberValue(42.0).build();
        Value boolVal = Value.newBuilder().setBoolValue(true).build();
        Value listVal = Value.newBuilder().setListValue(ListValue.newBuilder().addValues(stringVal).addValues(numberVal).build()).build();
        Struct struct = Struct.newBuilder().putFields("k", stringVal).build();
        Value structVal = Value.newBuilder().setStructValue(struct).build();
        assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(stringVal)).isEqualTo("str");
        assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(numberVal)).isEqualTo(42.0); // double
        assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(boolVal)).isEqualTo(true);
        assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(listVal)).isInstanceOf(java.util.List.class);
        assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(structVal)).isInstanceOf(java.util.Map.class);
        assertThat(PineconeStoreIterator.ProtobufValueConverter.convertProtobufValue(null)).isNull();
    }
}
