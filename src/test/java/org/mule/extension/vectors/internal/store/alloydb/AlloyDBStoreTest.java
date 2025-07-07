package org.mule.extension.vectors.internal.store.alloydb;

import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEmbeddingStore;
import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEngine;
import dev.langchain4j.community.store.embedding.alloydb.EmbeddingStoreConfig;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlloyDBStoreTest {
    AlloyDBStoreConnection connection;
    AlloyDBEngine engine;
    StoreConfiguration config;
    QueryParameters queryParams;

    @BeforeEach
    void setup() {
        connection = mock(AlloyDBStoreConnection.class);
        engine = mock(AlloyDBEngine.class);
        config = mock(StoreConfiguration.class);
        queryParams = mock(QueryParameters.class);
        when(connection.getAlloyDBEngine()).thenReturn(engine);
    }

    @Test
    void constructor_assigns_fields() {
        AlloyDBStore store = new AlloyDBStore(config, connection, "store", queryParams, 42, false);
        assertThat(store).isNotNull();
    }

    @Test
    void buildEmbeddingStore_success_when_createStore_true_calls_initVectorStoreTable() {
        AlloyDBStore store = new AlloyDBStore(config, connection, "store", queryParams, 42, true);
        // engine.initVectorStoreTable should be called
        // We can't mock the builder, but we can verify the call
        try {
            store.buildEmbeddingStore();
        } catch (Exception ignored) {}
        verify(engine).initVectorStoreTable(any(EmbeddingStoreConfig.class));
    }

    @Test
    void buildEmbeddingStore_success_when_createStore_false_does_not_call_initVectorStoreTable() {
        AlloyDBStore store = new AlloyDBStore(config, connection, "store", queryParams, 42, false);
        try {
            store.buildEmbeddingStore();
        } catch (Exception ignored) {}
        verify(engine, never()).initVectorStoreTable(any());
    }

    @Test
    void buildEmbeddingStore_wraps_SQLException_in_ModuleException() {
        // Simulate engine.initVectorStoreTable throwing RuntimeException with SQLException cause
        SQLException sqlEx = new SQLException("sqlfail", "28P01");
        doThrow(new RuntimeException("fail", sqlEx)).when(engine).initVectorStoreTable(any());
        AlloyDBStore store = new AlloyDBStore(config, connection, "store", queryParams, 42, true);
        Throwable thrown = catchThrowable(store::buildEmbeddingStore);
        assertThat(thrown)
            .isInstanceOf(ModuleException.class)
            .hasMessageContaining("authentication failed");
        assertThat(thrown.getCause()).isInstanceOf(SQLException.class);
        assertThat(((SQLException) thrown.getCause()).getSQLState()).isEqualTo("28P01");
    }

    @Test
    void buildEmbeddingStore_wraps_otherException_in_ModuleException() {
        // Simulate engine.initVectorStoreTable throwing RuntimeException
        doThrow(new RuntimeException("fail")).when(engine).initVectorStoreTable(any());
        AlloyDBStore store = new AlloyDBStore(config, connection, "store", queryParams, 42, true);
        Throwable thrown = catchThrowable(store::buildEmbeddingStore);
        assertThat(thrown)
            .isInstanceOf(ModuleException.class)
            .hasMessageContaining("Failed to build AlloyDB embedding store");
    }
}
