package org.mule.extension.vectors.internal.service.store;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BaseDatabaseIteratorTest {

  Connection connection;
  PreparedStatement pstmt;
  ResultSet resultSet;
  QueryParameters queryParams;

  @BeforeEach
  void setUp() throws Exception {
    connection = mock(Connection.class);
    pstmt = mock(PreparedStatement.class);
    resultSet = mock(ResultSet.class);
    queryParams = mock(QueryParameters.class);

    TestIterator.staticConn = connection;
    TestIterator.staticFields = new BaseDatabaseIterator.DatabaseFieldNames("id", "text", "metadata", "vector");

    when(connection.prepareStatement(anyString())).thenReturn(pstmt);
    when(pstmt.executeQuery()).thenReturn(resultSet);
    when(queryParams.retrieveEmbeddings()).thenReturn(false);
  }

  @Test
  void hasNext_returnsTrue_whenResultSetHasRows() throws Exception {
    when(resultSet.next()).thenReturn(true);
    try (TestIterator iter = new TestIterator("mytable", 10, queryParams)) {
      assertThat(iter.hasNext()).isTrue();
    }
  }

  @Test
  void hasNext_returnsFalse_whenNoMoreRowsOrPages() throws Exception {
    when(resultSet.next()).thenReturn(false);
    try (TestIterator iter = new TestIterator("mytable", 10, queryParams)) {
      assertThat(iter.hasNext()).isFalse();
    }
  }

  @Test
  void next_returnsResultSet() throws Exception {
    when(resultSet.next()).thenReturn(true);
    try (TestIterator iter = new TestIterator("mytable", 10, queryParams)) {
      assertThat(iter.next()).isSameAs(resultSet);
    }
  }

  @Test
  void next_throwsNoSuchElementException_whenEmpty() throws Exception {
    when(resultSet.next()).thenReturn(false);
    try (TestIterator iter = new TestIterator("mytable", 10, queryParams)) {
      assertThatThrownBy(iter::next).isInstanceOf(NoSuchElementException.class);
    }
  }

  @Test
  void pagination_fetchesNextPage_whenCurrentPageExhausted() throws Exception {
    when(resultSet.next()).thenReturn(true, false, true);
    try (TestIterator iter = new TestIterator("mytable", 10, queryParams)) {
      assertThat(iter.hasNext()).isTrue();
      iter.next();
      assertThat(iter.hasNext()).isTrue();

      verify(connection, times(2)).prepareStatement(anyString());
    }
  }

  @Test
  void close_closesAllResources() throws Exception {
    TestIterator iter = new TestIterator("mytable", 10, queryParams);
    iter.close();
    verify(resultSet).close();
    verify(pstmt).close();
    verify(connection).close();
  }

  @Test
  void close_handlesSQLExceptionGracefully() throws Exception {
    TestIterator iter = new TestIterator("mytable", 10, queryParams);
    doThrow(new SQLException("close failed")).when(resultSet).close();
    assertThatCode(iter::close).doesNotThrowAnyException();
  }

  @Test
  void handleSQLException_connectionState_throwsConnectionFailed() throws Exception {
    try (TestIterator iter = new TestIterator("mytable", 10, queryParams)) {
      SQLException ex = new SQLException("conn error", "08001");
      assertThatThrownBy(() -> iter.handleSQLException(ex))
          .isInstanceOf(ModuleException.class)
          .satisfies(thrown -> assertThat(((ModuleException) thrown).getType())
              .isEqualTo(MuleVectorsErrorType.CONNECTION_FAILED));
    }
  }

  @Test
  void handleSQLException_invalidPassword_throwsInvalidConnection() throws Exception {
    try (TestIterator iter = new TestIterator("mytable", 10, queryParams)) {
      SQLException ex = new SQLException("bad password", "28P01");
      assertThatThrownBy(() -> iter.handleSQLException(ex))
          .isInstanceOf(ModuleException.class)
          .satisfies(thrown -> assertThat(((ModuleException) thrown).getType())
              .isEqualTo(MuleVectorsErrorType.INVALID_CONNECTION));
    }
  }

  @Test
  void handleSQLException_nullState_throwsStoreServicesFailure() throws Exception {
    try (TestIterator iter = new TestIterator("mytable", 10, queryParams)) {
      SQLException ex = new SQLException("unknown error");
      assertThatThrownBy(() -> iter.handleSQLException(ex))
          .isInstanceOf(ModuleException.class)
          .satisfies(thrown -> assertThat(((ModuleException) thrown).getType())
              .isEqualTo(MuleVectorsErrorType.STORE_SERVICES_FAILURE));
    }
  }

  @Test
  void hasNext_throwsModuleException_onSQLException() throws Exception {
    when(resultSet.next()).thenThrow(new SQLException("db error"));
    try (TestIterator iter = new TestIterator("mytable", 10, queryParams)) {
      assertThatThrownBy(iter::hasNext)
          .isInstanceOf(ModuleException.class)
          .satisfies(thrown -> assertThat(((ModuleException) thrown).getType())
              .isEqualTo(MuleVectorsErrorType.STORE_SERVICES_FAILURE));
    }
  }

  @Test
  void fetchNextPage_withRetrieveEmbeddings_includesVectorField() throws Exception {
    when(queryParams.retrieveEmbeddings()).thenReturn(true);
    try (TestIterator ignored = new TestIterator("mytable", 10, queryParams)) {
      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      verify(connection).prepareStatement(captor.capture());
      assertThat(captor.getValue()).contains("vector");
    }
  }

  @Test
  void fetchNextPage_withoutRetrieveEmbeddings_excludesVectorField() throws Exception {
    try (TestIterator ignored = new TestIterator("mytable", 10, queryParams)) {
      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      verify(connection).prepareStatement(captor.capture());
      assertThat(captor.getValue()).doesNotContain("vector");
    }
  }

  static class TestIterator extends BaseDatabaseIterator {

    static Connection staticConn;
    static DatabaseFieldNames staticFields;

    TestIterator(String table, int pageSize, QueryParameters qp) throws SQLException {
      super(table, pageSize, qp);
    }

    @Override
    protected Connection getConnection() {
      return staticConn;
    }

    @Override
    protected DatabaseFieldNames getFieldNames() {
      return staticFields;
    }
  }
}
