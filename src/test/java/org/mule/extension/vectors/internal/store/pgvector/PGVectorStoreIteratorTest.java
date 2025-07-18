package org.mule.extension.vectors.internal.store.pgvector;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.provider.store.pgvector.PGVectorStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.pgvector.PGVectorStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PGVectorStoreIteratorTest {
    @Mock PGVectorStoreConnection pgVectorStoreConnection;
    @Mock DataSource dataSource;
    @Mock Connection connection;
    @Mock PreparedStatement pstmt;
    @Mock ResultSet resultSet;
    @Mock QueryParameters queryParameters;

    @BeforeEach
    void setUp() throws Exception {
        lenient().when(pgVectorStoreConnection.getDataSource()).thenReturn(dataSource);
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(queryParameters.pageSize()).thenReturn(2);
        lenient().when(queryParameters.retrieveEmbeddings()).thenReturn(true);
    }

    @Test
    void next_returnsVectorStoreRowWithCorrectFields() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("embedding_id")).thenReturn("id1");
        when(resultSet.getString("embedding")).thenReturn("{0.1,0.2,0.3}");
        when(resultSet.getString("text")).thenReturn("doc1");
        JSONObject meta = new JSONObject();
        meta.put("foo", "bar");
        when(resultSet.getString("metadata")).thenReturn(meta.toString());

        PGVectorStoreIterator<TextSegment>
            iterator = new PGVectorStoreIterator<>(pgVectorStoreConnection, "testStore", queryParameters);
        assertThat(iterator.hasNext()).isTrue();
        var row = iterator.next();
        assertThat(row.getId()).isEqualTo("id1");
        assertThat(row.getEmbedded()).isInstanceOf(TextSegment.class);
        assertThat(((TextSegment) row.getEmbedded()).text()).isEqualTo("doc1");
        assertThat(row.getEmbedding()).isInstanceOf(Embedding.class);
        assertThat(((Embedding) row.getEmbedding()).vector()).containsExactly(0.1f, 0.2f, 0.3f);
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    void next_whenNoMoreElements_throwsNoSuchElementException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        PGVectorStoreIterator<TextSegment> iterator = new PGVectorStoreIterator<>(pgVectorStoreConnection, "testStore", queryParameters);
        assertThat(iterator.hasNext()).isFalse();
        assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void constructor_whenSQLException_throwsModuleException() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("fail"));
        assertThatThrownBy(() -> new PGVectorStoreIterator<>(pgVectorStoreConnection, "testStore", queryParameters))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Store issue");
    }

    @Test
    void next_whenSQLException_throwsModuleException() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(anyString())).thenThrow(new SQLException("fail", "08S01"));
        PGVectorStoreIterator<TextSegment> iterator = new PGVectorStoreIterator<>(pgVectorStoreConnection, "testStore", queryParameters);
        assertThat(iterator.hasNext()).isTrue();
        assertThatThrownBy(iterator::next)
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Database connection failed");
    }
}
