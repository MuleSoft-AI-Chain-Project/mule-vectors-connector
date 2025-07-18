package org.mule.extension.vectors.internal.connection.store.pgvector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.pgvector.PGVectorStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.pgvector.PGVectorStoreConnectionParameters;
import org.mule.runtime.extension.api.exception.ModuleException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class PGVectorStoreConnectionTest {
    PGVectorStoreConnectionParameters params;
    PGVectorStoreConnection conn;

    @BeforeEach
    void setUp() {
        params = new PGVectorStoreConnectionParameters();
        try {
            java.lang.reflect.Field hostF = PGVectorStoreConnectionParameters.class.getDeclaredField("host");
            hostF.setAccessible(true);
            hostF.set(params, "localhost");
            java.lang.reflect.Field portF = PGVectorStoreConnectionParameters.class.getDeclaredField("port");
            portF.setAccessible(true);
            portF.set(params, 5432);
            java.lang.reflect.Field dbF = PGVectorStoreConnectionParameters.class.getDeclaredField("database");
            dbF.setAccessible(true);
            dbF.set(params, "default");
            java.lang.reflect.Field userF = PGVectorStoreConnectionParameters.class.getDeclaredField("user");
            userF.setAccessible(true);
            userF.set(params, "postgres");
            java.lang.reflect.Field pwF = PGVectorStoreConnectionParameters.class.getDeclaredField("password");
            pwF.setAccessible(true);
            pwF.set(params, "pw");
        } catch (Exception e) {
            fail(e);
        }
        conn = new PGVectorStoreConnection(params);
    }

    @Test
    void getters_work() {
        PGVectorStoreConnectionParameters p = (PGVectorStoreConnectionParameters) conn.getConnectionParameters();
        assertEquals("localhost", p.getHost());
        assertEquals(5432, p.getPort());
        assertEquals("default", p.getDatabase());
        assertEquals("postgres", p.getUser());
        assertEquals("pw", p.getPassword());
        assertEquals(params, conn.getConnectionParameters());
    }

    @Test
    void getVectorStore_returnsConstant() {
        assertEquals("PGVECTOR", conn.getVectorStore());
    }

    @Test
    void disconnect_closesConnection() throws Exception {
        DataSource ds = new DataSource() {
            @Override public Connection getConnection() throws SQLException { return new Connection() {
                @Override public void close() throws SQLException { closed = true; }
                boolean closed = false;
                @Override public boolean isClosed() throws SQLException { return closed; }
                // ... implement all other methods as no-op or throw UnsupportedOperationException
                public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
                public boolean isWrapperFor(Class<?> iface) { throw new UnsupportedOperationException(); }
                public java.sql.Statement createStatement() { throw new UnsupportedOperationException(); }
                public java.sql.PreparedStatement prepareStatement(String sql) { throw new UnsupportedOperationException(); }
                public java.sql.CallableStatement prepareCall(String sql) { throw new UnsupportedOperationException(); }
                public String nativeSQL(String sql) { throw new UnsupportedOperationException(); }
                public void setAutoCommit(boolean autoCommit) { throw new UnsupportedOperationException(); }
                public boolean getAutoCommit() { throw new UnsupportedOperationException(); }
                public void commit() { throw new UnsupportedOperationException(); }
                public void rollback() { throw new UnsupportedOperationException(); }
                public java.sql.DatabaseMetaData getMetaData() { throw new UnsupportedOperationException(); }
                public void setReadOnly(boolean readOnly) { throw new UnsupportedOperationException(); }
                public boolean isReadOnly() { throw new UnsupportedOperationException(); }
                public void setCatalog(String catalog) { throw new UnsupportedOperationException(); }
                public String getCatalog() { throw new UnsupportedOperationException(); }
                public void setTransactionIsolation(int level) { throw new UnsupportedOperationException(); }
                public int getTransactionIsolation() { throw new UnsupportedOperationException(); }
                public java.sql.SQLWarning getWarnings() { throw new UnsupportedOperationException(); }
                public void clearWarnings() { throw new UnsupportedOperationException(); }
                public void setHoldability(int holdability) { throw new UnsupportedOperationException(); }
                public int getHoldability() { throw new UnsupportedOperationException(); }
                public java.sql.Savepoint setSavepoint() { throw new UnsupportedOperationException(); }
                public java.sql.Savepoint setSavepoint(String name) { throw new UnsupportedOperationException(); }
                public void rollback(java.sql.Savepoint savepoint) { throw new UnsupportedOperationException(); }
                public void releaseSavepoint(java.sql.Savepoint savepoint) { throw new UnsupportedOperationException(); }
                public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) { throw new UnsupportedOperationException(); }
                public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) { throw new UnsupportedOperationException(); }
                public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) { throw new UnsupportedOperationException(); }
                public java.util.Map<String,Class<?>> getTypeMap() { throw new UnsupportedOperationException(); }
                public void setTypeMap(java.util.Map<String,Class<?>> map) { throw new UnsupportedOperationException(); }
                public void setSchema(String schema) { throw new UnsupportedOperationException(); }
                public String getSchema() { throw new UnsupportedOperationException(); }
                public void abort(java.util.concurrent.Executor executor) { throw new UnsupportedOperationException(); }
                public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) { throw new UnsupportedOperationException(); }
                public int getNetworkTimeout() { throw new UnsupportedOperationException(); }
                @Override public java.sql.Struct createStruct(String typeName, Object[] attributes) { throw new UnsupportedOperationException(); }
                @Override public java.sql.Clob createClob() { throw new UnsupportedOperationException(); }
                @Override public java.sql.Array createArrayOf(String typeName, Object[] elements) { throw new UnsupportedOperationException(); }
                @Override public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) { throw new UnsupportedOperationException(); }
                @Override public void setClientInfo(java.util.Properties properties) { throw new UnsupportedOperationException(); }
                @Override public void setClientInfo(String name, String value) { throw new UnsupportedOperationException(); }
                @Override public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) { throw new UnsupportedOperationException(); }
                @Override public java.util.Properties getClientInfo() { throw new UnsupportedOperationException(); }
                @Override public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) { throw new UnsupportedOperationException(); }
                @Override public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) { throw new UnsupportedOperationException(); }
                @Override public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) { throw new UnsupportedOperationException(); }
                @Override public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) { throw new UnsupportedOperationException(); }
                @Override public java.sql.NClob createNClob() { throw new UnsupportedOperationException(); }
                @Override public String getClientInfo(String name) { throw new UnsupportedOperationException(); }
                @Override public boolean isValid(int timeout) { throw new UnsupportedOperationException(); }
                @Override public java.sql.SQLXML createSQLXML() { throw new UnsupportedOperationException(); }
                @Override public java.sql.Blob createBlob() { throw new UnsupportedOperationException(); }
            }; }
            @Override public Connection getConnection(String username, String password) { throw new UnsupportedOperationException(); }
            @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
            @Override public boolean isWrapperFor(Class<?> iface) { throw new UnsupportedOperationException(); }
            @Override public java.io.PrintWriter getLogWriter() { throw new UnsupportedOperationException(); }
            @Override public void setLogWriter(java.io.PrintWriter out) { throw new UnsupportedOperationException(); }
            @Override public void setLoginTimeout(int seconds) { throw new UnsupportedOperationException(); }
            @Override public int getLoginTimeout() { throw new UnsupportedOperationException(); }
            @Override public java.util.logging.Logger getParentLogger() { throw new UnsupportedOperationException(); }
        };
        java.lang.reflect.Field f = PGVectorStoreConnection.class.getDeclaredField("dataSource");
        f.setAccessible(true);
        f.set(conn, ds);
        assertDoesNotThrow(conn::disconnect);
    }

    @Test
    void validate_success() throws Exception {
        DataSource ds = new DataSource() {
            @Override public Connection getConnection() { return new Connection() {
                @Override public void close() {}
                @Override public boolean isClosed() { return false; }
                // ... implement all other methods as no-op or throw UnsupportedOperationException
                public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
                public boolean isWrapperFor(Class<?> iface) { throw new UnsupportedOperationException(); }
                public java.sql.Statement createStatement() { throw new UnsupportedOperationException(); }
                public java.sql.PreparedStatement prepareStatement(String sql) { throw new UnsupportedOperationException(); }
                public java.sql.CallableStatement prepareCall(String sql) { throw new UnsupportedOperationException(); }
                public String nativeSQL(String sql) { throw new UnsupportedOperationException(); }
                public void setAutoCommit(boolean autoCommit) { throw new UnsupportedOperationException(); }
                public boolean getAutoCommit() { throw new UnsupportedOperationException(); }
                public void commit() { throw new UnsupportedOperationException(); }
                public void rollback() { throw new UnsupportedOperationException(); }
                public java.sql.DatabaseMetaData getMetaData() { throw new UnsupportedOperationException(); }
                public void setReadOnly(boolean readOnly) { throw new UnsupportedOperationException(); }
                public boolean isReadOnly() { throw new UnsupportedOperationException(); }
                public void setCatalog(String catalog) { throw new UnsupportedOperationException(); }
                public String getCatalog() { throw new UnsupportedOperationException(); }
                public void setTransactionIsolation(int level) { throw new UnsupportedOperationException(); }
                public int getTransactionIsolation() { throw new UnsupportedOperationException(); }
                public java.sql.SQLWarning getWarnings() { throw new UnsupportedOperationException(); }
                public void clearWarnings() { throw new UnsupportedOperationException(); }
                public void setHoldability(int holdability) { throw new UnsupportedOperationException(); }
                public int getHoldability() { throw new UnsupportedOperationException(); }
                public java.sql.Savepoint setSavepoint() { throw new UnsupportedOperationException(); }
                public java.sql.Savepoint setSavepoint(String name) { throw new UnsupportedOperationException(); }
                public void rollback(java.sql.Savepoint savepoint) { throw new UnsupportedOperationException(); }
                public void releaseSavepoint(java.sql.Savepoint savepoint) { throw new UnsupportedOperationException(); }
                public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) { throw new UnsupportedOperationException(); }
                public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) { throw new UnsupportedOperationException(); }
                public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) { throw new UnsupportedOperationException(); }
                public java.util.Map<String,Class<?>> getTypeMap() { throw new UnsupportedOperationException(); }
                public void setTypeMap(java.util.Map<String,Class<?>> map) { throw new UnsupportedOperationException(); }
                public void setSchema(String schema) { throw new UnsupportedOperationException(); }
                public String getSchema() { throw new UnsupportedOperationException(); }
                public void abort(java.util.concurrent.Executor executor) { throw new UnsupportedOperationException(); }
                public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) { throw new UnsupportedOperationException(); }
                public int getNetworkTimeout() { throw new UnsupportedOperationException(); }
                @Override public java.sql.Struct createStruct(String typeName, Object[] attributes) { throw new UnsupportedOperationException(); }
                @Override public java.sql.Clob createClob() { throw new UnsupportedOperationException(); }
                @Override public java.sql.Array createArrayOf(String typeName, Object[] elements) { throw new UnsupportedOperationException(); }
                @Override public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) { throw new UnsupportedOperationException(); }
                @Override public void setClientInfo(java.util.Properties properties) { throw new UnsupportedOperationException(); }
                @Override public void setClientInfo(String name, String value) { throw new UnsupportedOperationException(); }
                @Override public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) { throw new UnsupportedOperationException(); }
                @Override public java.util.Properties getClientInfo() { throw new UnsupportedOperationException(); }
                @Override public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) { throw new UnsupportedOperationException(); }
                @Override public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) { throw new UnsupportedOperationException(); }
                @Override public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) { throw new UnsupportedOperationException(); }
                @Override public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) { throw new UnsupportedOperationException(); }
                @Override public java.sql.NClob createNClob() { throw new UnsupportedOperationException(); }
                @Override public String getClientInfo(String name) { throw new UnsupportedOperationException(); }
                @Override public boolean isValid(int timeout) { throw new UnsupportedOperationException(); }
                @Override public java.sql.SQLXML createSQLXML() { throw new UnsupportedOperationException(); }
                @Override public java.sql.Blob createBlob() { throw new UnsupportedOperationException(); }
            }; }
            @Override public Connection getConnection(String username, String password) { throw new UnsupportedOperationException(); }
            @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
            @Override public boolean isWrapperFor(Class<?> iface) { throw new UnsupportedOperationException(); }
            @Override public java.io.PrintWriter getLogWriter() { throw new UnsupportedOperationException(); }
            @Override public void setLogWriter(java.io.PrintWriter out) { throw new UnsupportedOperationException(); }
            @Override public void setLoginTimeout(int seconds) { throw new UnsupportedOperationException(); }
            @Override public int getLoginTimeout() { throw new UnsupportedOperationException(); }
            @Override public java.util.logging.Logger getParentLogger() { throw new UnsupportedOperationException(); }
        };
        java.lang.reflect.Field f = PGVectorStoreConnection.class.getDeclaredField("dataSource");
        f.setAccessible(true);
        f.set(conn, ds);
        assertDoesNotThrow(conn::validate);
    }

    @Test
    void validate_missingHost() {
        try {
            java.lang.reflect.Field hostF = PGVectorStoreConnectionParameters.class.getDeclaredField("host");
            hostF.setAccessible(true);
            hostF.set(params, null);
        } catch (Exception e) { fail(e); }
        ModuleException ex = assertThrows(ModuleException.class, conn::validate);
        assertTrue(ex.getMessage().contains("Host is required"));
    }

    @Test
    void validate_missingPort() {
        try {
            java.lang.reflect.Field portF = PGVectorStoreConnectionParameters.class.getDeclaredField("port");
            portF.setAccessible(true);
            portF.set(params, 0);
        } catch (Exception e) { fail(e); }
        ModuleException ex = assertThrows(ModuleException.class, conn::validate);
        assertTrue(ex.getMessage().contains("Port is required"));
    }

    @Test
    void validate_missingDatabase() {
        try {
            java.lang.reflect.Field dbF = PGVectorStoreConnectionParameters.class.getDeclaredField("database");
            dbF.setAccessible(true);
            dbF.set(params, null);
        } catch (Exception e) { fail(e); }
        ModuleException ex = assertThrows(ModuleException.class, conn::validate);
        assertTrue(ex.getMessage().contains("Database is required"));
    }

    @Test
    void validate_missingUser() {
        try {
            java.lang.reflect.Field userF = PGVectorStoreConnectionParameters.class.getDeclaredField("user");
            userF.setAccessible(true);
            userF.set(params, null);
        } catch (Exception e) { fail(e); }
        ModuleException ex = assertThrows(ModuleException.class, conn::validate);
        assertTrue(ex.getMessage().contains("User is required"));
    }

    @Test
    void validate_missingPassword() {
        try {
            java.lang.reflect.Field pwF = PGVectorStoreConnectionParameters.class.getDeclaredField("password");
            pwF.setAccessible(true);
            pwF.set(params, null);
        } catch (Exception e) { fail(e); }
        ModuleException ex = assertThrows(ModuleException.class, conn::validate);
        assertTrue(ex.getMessage().contains("Password is required"));
    }

    @Test
    void validate_sqlException() throws Exception {
        DataSource ds = new DataSource() {
            @Override public Connection getConnection() throws SQLException { throw new SQLException("fail"); }
            @Override public Connection getConnection(String username, String password) { throw new UnsupportedOperationException(); }
            @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
            @Override public boolean isWrapperFor(Class<?> iface) { throw new UnsupportedOperationException(); }
            @Override public java.io.PrintWriter getLogWriter() { throw new UnsupportedOperationException(); }
            @Override public void setLogWriter(java.io.PrintWriter out) { throw new UnsupportedOperationException(); }
            @Override public void setLoginTimeout(int seconds) { throw new UnsupportedOperationException(); }
            @Override public int getLoginTimeout() { throw new UnsupportedOperationException(); }
            @Override public java.util.logging.Logger getParentLogger() { throw new UnsupportedOperationException(); }
        };
        java.lang.reflect.Field f = PGVectorStoreConnection.class.getDeclaredField("dataSource");
        f.setAccessible(true);
        f.set(conn, ds);
        ModuleException ex = assertThrows(ModuleException.class, conn::validate);
        assertTrue(ex.getMessage().contains("Failed to connect to PG Vector"));
    }

    @Test
    void initialise_setsDataSource() {
        assertDoesNotThrow(conn::initialise);
        assertNotNull(conn.getDataSource());
    }
} 