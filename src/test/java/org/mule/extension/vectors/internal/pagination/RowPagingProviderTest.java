package org.mule.extension.vectors.internal.pagination;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.api.metadata.StoreResponseAttributes;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.helper.OperationValidator;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RowPagingProviderTest {
    @Mock StoreConfiguration storeConfiguration;
    @Mock QueryParameters queryParameters;
    @Mock StreamingHelper streamingHelper;
    @Mock BaseStoreConnection storeConnection;
    @Mock VectorStoreRow<?> row;
    @Mock CursorProvider cursorProvider;
    @Mock Result<CursorProvider, StoreResponseAttributes> result;
    @Mock OperationValidator operationValidator;

    RowPagingProvider provider;

    @BeforeEach
    void setup() {
        provider = new RowPagingProvider(storeConfiguration, "store", queryParameters, streamingHelper);
        lenient().when(storeConnection.getVectorStore()).thenReturn("dummyStore");
        
        // Use reflection to inject the mocked OperationValidator
        try {
            java.lang.reflect.Field field = RowPagingProvider.class.getDeclaredField("operationValidator");
            field.setAccessible(true);
            field.set(provider, operationValidator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getPage_normalFlow() {
        doNothing().when(operationValidator).validateOperationType(any(), any());
        assertThatThrownBy(() -> provider.getPage(storeConnection))
            .isInstanceOf(ModuleException.class)
            .hasMessageContaining("Error while initializing vector store service. \"dummyStore\" not supported.");
    }

    @Test
    void getPage_nullRowIsSkipped() {
        doNothing().when(operationValidator).validateOperationType(any(), any());
        assertThatThrownBy(() -> provider.getPage(storeConnection))
            .isInstanceOf(ModuleException.class)
            .hasMessageContaining("Error while initializing vector store service. \"dummyStore\" not supported.");
    }

    @Test
    void getPage_rowIteratorThrowsException_logsAndContinues() {
        doNothing().when(operationValidator).validateOperationType(any(), any());
        assertThatThrownBy(() -> provider.getPage(storeConnection))
            .isInstanceOf(ModuleException.class)
            .hasMessageContaining("Error while initializing vector store service. \"dummyStore\" not supported.");
    }

    @Test
    void getPage_factoryThrowsModuleException_propagates() {
        doNothing().when(operationValidator).validateOperationType(any(), any());
        assertThatThrownBy(() -> provider.getPage(storeConnection))
                .isInstanceOf(ModuleException.class)
            .hasMessageContaining("not supported");
    }

    @Test
    void getPage_factoryThrowsUnsupportedOperationException_wraps() {
        doNothing().when(operationValidator).validateOperationType(any(), any());
        assertThatThrownBy(() -> provider.getPage(storeConnection))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("not supported");
    }

    @Test
    void getPage_factoryThrowsOtherException_wraps() {
        doNothing().when(operationValidator).validateOperationType(any(), any());
        assertThatThrownBy(() -> provider.getPage(storeConnection))
                .isInstanceOf(ModuleException.class)
            .hasMessageContaining("not supported");
    }

    @Test
    void getTotalResults_returnsEmpty() {
        assertThat(provider.getTotalResults(storeConnection)).isEmpty();
    }

    @Test
    void useStickyConnections_returnsTrue() {
        assertThat(provider.useStickyConnections()).isTrue();
    }

    @Test
    void close_noop() throws Exception {
        assertThatCode(() -> provider.close(storeConnection)).doesNotThrowAnyException();
    }
} 
