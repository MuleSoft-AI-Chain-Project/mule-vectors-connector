package org.mule.extension.vectors.internal.store.alloydb;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.connection.provider.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.alloydb.AlloyDBStoreIterator;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import dev.langchain4j.community.store.embedding.alloydb.AlloyDBEngine;
import dev.langchain4j.data.segment.TextSegment;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AlloyDBStoreIteratorTest {

  AlloyDBStoreConnection connection;
  QueryParameters queryParameters;
  Connection jdbcConnection;
  PreparedStatement pstmt;
  ResultSet resultSet;
  AlloyDBEngine engine;

  @BeforeEach
  void setup() throws Exception {
    connection = mock(AlloyDBStoreConnection.class, CALLS_REAL_METHODS);
    queryParameters = mock(QueryParameters.class);
    jdbcConnection = mock(Connection.class);
    pstmt = mock(PreparedStatement.class);
    resultSet = mock(ResultSet.class);
    engine = mock(AlloyDBEngine.class);
    // Inject mock engine into connection using reflection
    Field field = connection.getClass().getDeclaredField("alloyDBEngine");
    field.setAccessible(true);
    field.set(connection, engine);
    when(connection.getAlloyDBEngine()).thenReturn(engine);
    when(queryParameters.pageSize()).thenReturn(1);
    when(queryParameters.retrieveEmbeddings()).thenReturn(true);
  }

  @Test
  void hasNext_and_next_work_for_valid_result() throws SQLException {
    when(engine.getConnection()).thenReturn(jdbcConnection);
    when(jdbcConnection.prepareStatement(anyString())).thenReturn(pstmt);
    when(pstmt.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getString("langchain_id")).thenReturn("id1");
    when(resultSet.getString("content")).thenReturn("hello");
    when(resultSet.getString("langchain_metadata")).thenReturn(new JSONObject().put("foo", "bar").toString());
    when(resultSet.getString("embedding")).thenReturn("{1.0,2.0,3.0}");

    AlloyDBStoreIterator<TextSegment> iterator = new AlloyDBStoreIterator<>(connection, "table", queryParameters);
    var row = iterator.next();
    assertThat(row.getId()).isEqualTo("id1");
    assertThat(row.getEmbedding().vector()).containsExactly(1.0f, 2.0f, 3.0f);
    assertThat(((TextSegment) row.getEmbedded()).text()).isEqualTo("hello");
    assertThat(((TextSegment) row.getEmbedded()).metadata().getString("foo")).isEqualTo("bar");
  }

  @Test
  void next_throws_NoSuchElementException_when_out_of_bounds() throws SQLException {
    when(engine.getConnection()).thenReturn(jdbcConnection);
    when(jdbcConnection.prepareStatement(anyString())).thenReturn(pstmt);
    when(pstmt.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);
    when(resultSet.getString(anyString())).thenThrow(new AssertionError("getString should not be called when no results"));

    AlloyDBStoreIterator<TextSegment> iterator = new AlloyDBStoreIterator<>(connection, "table", queryParameters);
    assertThat(iterator.hasNext()).isFalse();
    assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void constructor_throws_ModuleException_on_auth_error() throws SQLException {
    when(engine.getConnection()).thenReturn(jdbcConnection);
    when(jdbcConnection.prepareStatement(anyString())).thenThrow(new SQLException("auth fail", "28P01"));
    when(connection.getAlloyDBEngine()).thenReturn(engine);

    assertThatThrownBy(() -> new AlloyDBStoreIterator<>(connection, "table", queryParameters))
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("Authentication failed");
  }

  @Test
  void next_throws_ModuleException_on_sql_error() throws SQLException {
    when(engine.getConnection()).thenReturn(jdbcConnection);
    when(jdbcConnection.prepareStatement(anyString())).thenReturn(pstmt);
    when(pstmt.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getString(anyString())).thenThrow(new SQLException("fail", "08001"));

    AlloyDBStoreIterator<TextSegment> iterator = new AlloyDBStoreIterator<>(connection, "table", queryParameters);
    assertThat(iterator.hasNext()).isTrue();
    assertThatThrownBy(iterator::next).isInstanceOf(ModuleException.class)
        .hasMessageContaining("Database connection failed");
  }
}
