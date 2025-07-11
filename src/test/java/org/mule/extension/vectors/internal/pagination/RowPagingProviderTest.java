package org.mule.extension.vectors.internal.pagination;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.api.metadata.StoreResponseAttributes;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.VectoreStoreIterator;
import org.mule.extension.vectors.internal.service.VectorStoreServiceProviderFactory;
import org.mule.extension.vectors.internal.store.VectorStoreRow;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;
import org.mule.extension.vectors.internal.helper.OperationValidator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RowPagingProviderTest {
    @Mock StoreConfiguration storeConfiguration;
    @Mock QueryParameters queryParameters;
    @Mock StreamingHelper streamingHelper;
    @Mock BaseStoreConnection storeConnection;
    @Mock VectoreStoreIterator iterator;
    @Mock VectorStoreRow<?> row;
    @Mock CursorProvider cursorProvider;
    @Mock Result<CursorProvider, StoreResponseAttributes> result;

    RowPagingProvider provider;

    @BeforeEach
    void setup() {
        provider = new RowPagingProvider(storeConfiguration, "store", queryParameters, streamingHelper);
        lenient().when(storeConnection.getVectorStore()).thenReturn("dummyStore");
    }

    @Test
    void getPage_normalFlow() {
        try (MockedStatic<OperationValidator> validator = Mockito.mockStatic(OperationValidator.class)) {
            validator.when(() -> OperationValidator.validateOperationType(any(), any())).thenAnswer(invocation -> null);
            try (MockedStatic<VectorStoreServiceProviderFactory> factory = Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {
                when(iterator.hasNext()).thenReturn(true, false);
                when(iterator.next()).thenReturn(row);
                var providerMock = mock(org.mule.extension.vectors.internal.service.VectorStoreServiceProvider.class);
                when(providerMock.getFileIterator()).thenReturn(iterator);
                factory.when(() -> VectorStoreServiceProviderFactory.getInstance(any(), any(), any(), any(), anyInt(), anyBoolean())).thenReturn(providerMock);
                List<Result<CursorProvider, StoreResponseAttributes>> page = provider.getPage(storeConnection);
                assertThat(page).isNotNull();
            }
        }
    }

    @Test
    void getPage_nullRowIsSkipped() {
        try (MockedStatic<OperationValidator> validator = Mockito.mockStatic(OperationValidator.class)) {
            validator.when(() -> OperationValidator.validateOperationType(any(), any())).thenAnswer(invocation -> null);
            try (MockedStatic<VectorStoreServiceProviderFactory> factory = Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {
                when(iterator.hasNext()).thenReturn(true, false);
                when(iterator.next()).thenReturn(null);
                var providerMock = mock(org.mule.extension.vectors.internal.service.VectorStoreServiceProvider.class);
                when(providerMock.getFileIterator()).thenReturn(iterator);
                factory.when(() -> VectorStoreServiceProviderFactory.getInstance(any(), any(), any(), any(), anyInt(), anyBoolean())).thenReturn(providerMock);
                List<Result<CursorProvider, StoreResponseAttributes>> page = provider.getPage(storeConnection);
                assertThat(page).isEmpty();
            }
        }
    }

    @Test
    void getPage_rowIteratorThrowsException_logsAndContinues() {
        try (MockedStatic<OperationValidator> validator = Mockito.mockStatic(OperationValidator.class)) {
            validator.when(() -> OperationValidator.validateOperationType(any(), any())).thenAnswer(invocation -> null);
            try (MockedStatic<VectorStoreServiceProviderFactory> factory = Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {
                when(iterator.hasNext()).thenReturn(true, false);
                when(iterator.next()).thenThrow(new RuntimeException("fail"));
                var providerMock = mock(org.mule.extension.vectors.internal.service.VectorStoreServiceProvider.class);
                when(providerMock.getFileIterator()).thenReturn(iterator);
                factory.when(() -> VectorStoreServiceProviderFactory.getInstance(any(), any(), any(), any(), anyInt(), anyBoolean())).thenReturn(providerMock);
                List<Result<CursorProvider, StoreResponseAttributes>> page = provider.getPage(storeConnection);
                assertThat(page).isEmpty();
            }
        }
    }

    @Test
    void getPage_factoryThrowsModuleException_propagates() {
        try (MockedStatic<OperationValidator> validator = Mockito.mockStatic(OperationValidator.class)) {
            validator.when(() -> OperationValidator.validateOperationType(any(), any())).thenAnswer(invocation -> null);
            try (MockedStatic<VectorStoreServiceProviderFactory> factory = Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {
                factory.when(() -> VectorStoreServiceProviderFactory.getInstance(any(), any(), any(), any(), anyInt(), anyBoolean()))
                        .thenThrow(new ModuleException("fail", org.mule.extension.vectors.internal.error.MuleVectorsErrorType.STORE_SERVICES_FAILURE));
                assertThatThrownBy(() -> provider.getPage(storeConnection))
                        .isInstanceOf(ModuleException.class)
                        .hasMessageContaining("fail");
            }
        }
    }

    @Test
    void getPage_factoryThrowsUnsupportedOperationException_wraps() {
        try (MockedStatic<OperationValidator> validator = Mockito.mockStatic(OperationValidator.class)) {
            validator.when(() -> OperationValidator.validateOperationType(any(), any())).thenAnswer(invocation -> null);
            try (MockedStatic<VectorStoreServiceProviderFactory> factory = Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {
                factory.when(() -> VectorStoreServiceProviderFactory.getInstance(any(), any(), any(), any(), anyInt(), anyBoolean()))
                        .thenThrow(new UnsupportedOperationException("not supported"));
                assertThatThrownBy(() -> provider.getPage(storeConnection))
                        .isInstanceOf(ModuleException.class)
                        .hasMessageContaining("not supported");
            }
        }
    }

    @Test
    void getPage_factoryThrowsOtherException_wraps() {
        try (MockedStatic<OperationValidator> validator = Mockito.mockStatic(OperationValidator.class)) {
            validator.when(() -> OperationValidator.validateOperationType(any(), any())).thenAnswer(invocation -> null);
            try (MockedStatic<VectorStoreServiceProviderFactory> factory = Mockito.mockStatic(VectorStoreServiceProviderFactory.class)) {
                factory.when(() -> VectorStoreServiceProviderFactory.getInstance(any(), any(), any(), any(), anyInt(), anyBoolean()))
                        .thenThrow(new RuntimeException("boom"));
                assertThatThrownBy(() -> provider.getPage(storeConnection))
                        .isInstanceOf(ModuleException.class)
                        .hasMessageContaining("Error while getting row from");
            }
        }
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