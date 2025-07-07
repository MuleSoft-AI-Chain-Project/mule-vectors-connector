package org.mule.extension.vectors.internal.store.pinecone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import io.pinecone.clients.Pinecone;
import io.pinecone.clients.Index;
import io.pinecone.proto.ListResponse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PineconeStoreServiceProviderTest {
    @Mock StoreConfiguration storeConfiguration;
    @Mock PineconeStoreConnection pineconeStoreConnection;
    @Mock QueryParameters queryParameters;

    private PineconeStoreServiceProvider provider;

    @BeforeEach
    void setup() {
        provider = new PineconeStoreServiceProvider(
                storeConfiguration, pineconeStoreConnection, "storeName", queryParameters, 42, true);
    }

    @Test
    void constructor_assignsFields() {
        assertThat(provider).extracting(
                "storeConfiguration", "connection", "storeName", "queryParams", "dimension", "createStore")
                .containsExactly(
                        storeConfiguration, pineconeStoreConnection, "storeName", queryParameters, 42, true
                );
    }

    @Test
    void getService_returnsPineconeStoreWithCorrectParams() {
        PineconeStoreServiceProvider localProvider = new PineconeStoreServiceProvider(
                storeConfiguration, pineconeStoreConnection, "myStore", queryParameters, 99, false);
        var service = localProvider.getService();
        assertThat(service).isInstanceOf(PineconeStore.class);
        PineconeStore store = (PineconeStore) service;
        assertThat(store).extracting("apiKey", "cloud", "region", "queryParams")
                .contains(
                        pineconeStoreConnection.getApiKey(),
                        pineconeStoreConnection.getCloud(),
                        pineconeStoreConnection.getRegion(),
                        queryParameters
                );
    }

    @Test
    void getFileIterator_returnsPineconeStoreIteratorWithCorrectParams() {
        // Fully mock Pinecone client and Index to prevent NPE in iterator
        Pinecone mockPinecone = mock(Pinecone.class);
        Index mockIndex = mock(Index.class);
        ListResponse mockListResponse = mock(ListResponse.class);
        io.pinecone.proto.Pagination mockPagination = mock(io.pinecone.proto.Pagination.class);
        when(mockListResponse.getPagination()).thenReturn(mockPagination);
        when(mockPagination.getNext()).thenReturn("");
        when(pineconeStoreConnection.getClient()).thenReturn(mockPinecone);
        when(mockPinecone.getIndexConnection(anyString())).thenReturn(mockIndex);
        when(mockIndex.list(anyString(), anyInt())).thenReturn(mockListResponse);
        PineconeStoreServiceProvider localProvider = new PineconeStoreServiceProvider(
                storeConfiguration, pineconeStoreConnection, "myStore", queryParameters, 99, false);
        var iterator = localProvider.getFileIterator();
        assertThat(iterator).isInstanceOf(PineconeStoreIterator.class);
        PineconeStoreIterator<?> pineconeIterator = (PineconeStoreIterator<?>) iterator;
        // Use reflection to check private fields
        try {
            java.lang.reflect.Field storeNameField = pineconeIterator.getClass().getDeclaredField("storeName");
            storeNameField.setAccessible(true);
            assertThat(storeNameField.get(pineconeIterator)).isEqualTo("myStore");
            java.lang.reflect.Field queryParamsField = pineconeIterator.getClass().getDeclaredField("queryParams");
            queryParamsField.setAccessible(true);
            assertThat(queryParamsField.get(pineconeIterator)).isEqualTo(queryParameters);
            java.lang.reflect.Field connField = pineconeIterator.getClass().getDeclaredField("pineconeStoreConnection");
            connField.setAccessible(true);
            assertThat(connField.get(pineconeIterator)).isEqualTo(pineconeStoreConnection);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}
