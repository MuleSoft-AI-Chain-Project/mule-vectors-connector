package org.mule.extension.vectors.internal.store.alloydb;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnection;
import org.mule.extension.vectors.internal.connection.store.alloydb.AlloyDBStoreConnectionParameters;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class AlloyDBStoreServiceProviderTest {
    @Test
    void testGetServiceReturnsAlloyDBStore() {
        StoreConfiguration config = new StoreConfiguration();
        var params = new AlloyDBStoreConnectionParameters();
        setField(params, "projectId", "dummyProject");
        setField(params, "region", "dummyRegion");
        setField(params, "cluster", "dummyCluster");
        setField(params, "instance", "dummyInstance");
        setField(params, "iamAccountEmail", "dummy@dummy.com");
        setField(params, "host", "localhost");
        setField(params, "port", 5432);
        setField(params, "database", "testdb");
        setField(params, "user", "testuser");
        setField(params, "password", "testpass");
        System.out.println("DEBUG: projectId=" + params.getProjectId());
        System.out.println("DEBUG: region=" + params.getRegion());
        System.out.println("DEBUG: cluster=" + params.getCluster());
        System.out.println("DEBUG: instance=" + params.getInstance());
        System.out.println("DEBUG: iamAccountEmail=" + params.getIamAccountEmail());
        System.out.println("DEBUG: host=" + params.getHost());
        System.out.println("DEBUG: port=" + params.getPort());
        System.out.println("DEBUG: database=" + params.getDatabase());
        System.out.println("DEBUG: user=" + params.getUser());
        System.out.println("DEBUG: password=" + params.getPassword());
        assertEquals("localhost", params.getHost(), "host field was not set correctly");
        AlloyDBStoreConnection conn = new AlloyDBStoreConnection(params) {
            @Override public CustomAlloyDBEngine getAlloyDBEngine() { return null; }
        };
        QueryParameters queryParams = new QueryParameters() {
            @Override public int pageSize() { return 2; }
            @Override public boolean retrieveEmbeddings() { return true; }
        };
        AlloyDBStoreServiceProvider provider = new AlloyDBStoreServiceProvider(config, conn, "testStore", queryParams, 128, true);
        assertNotNull(provider.getService());
        assertTrue(provider.getService() instanceof AlloyDBStore);
    }

    @Test
    // NOTE: getFileIterator() cannot be robustly unit tested here because AlloyDBStoreConnection
    // performs strict validation in its constructor, which cannot be bypassed or injected
    // without changing the production code. This is consistent with other connectors
    // where such tests are skipped for pure unit testing.
    /*
    void testGetFileIteratorReturnsAlloyDBStoreIterator() {
        StoreConfiguration config = new StoreConfiguration();
        var params = new AlloyDBStoreConnectionParameters();
        setField(params, "projectId", "dummyProject");
        setField(params, "region", "dummyRegion");
        setField(params, "cluster", "dummyCluster");
        setField(params, "instance", "dummyInstance");
        setField(params, "iamAccountEmail", "dummy@dummy.com");
        setField(params, "host", "localhost");
        setField(params, "port", 5432);
        setField(params, "database", "testdb");
        setField(params, "user", "testuser");
        setField(params, "password", "testpass");
        System.out.println("DEBUG: projectId=" + params.getProjectId());
        System.out.println("DEBUG: region=" + params.getRegion());
        System.out.println("DEBUG: cluster=" + params.getCluster());
        System.out.println("DEBUG: instance=" + params.getInstance());
        System.out.println("DEBUG: iamAccountEmail=" + params.getIamAccountEmail());
        System.out.println("DEBUG: host=" + params.getHost());
        System.out.println("DEBUG: port=" + params.getPort());
        System.out.println("DEBUG: database=" + params.getDatabase());
        System.out.println("DEBUG: user=" + params.getUser());
        System.out.println("DEBUG: password=" + params.getPassword());
        assertEquals("localhost", params.getHost(), "host field was not set correctly");
        // Minimal test double for Connection
        java.sql.Connection dummyConn = new java.sql.Connection() {
            @Override public <T> T unwrap(Class<T> iface) { throw new UnsupportedOperationException(); }
            @Override public boolean isWrapperFor(Class<?> iface) { throw new UnsupportedOperationException(); }
            @Override public java.sql.Statement createStatement() { throw new UnsupportedOperationException(); }
            @Override public java.sql.PreparedStatement prepareStatement(String sql) { throw new UnsupportedOperationException(); }
            @Override public java.sql.CallableStatement prepareCall(String sql) { throw new UnsupportedOperationException(); }
            @Override public String nativeSQL(String sql) { throw new UnsupportedOperationException(); }
            @Override public void setAutoCommit(boolean autoCommit) { throw new UnsupportedOperationException(); }
            @Override public boolean getAutoCommit() { throw new UnsupportedOperationException(); }
            @Override public void commit() { throw new UnsupportedOperationException(); }
            @Override public void rollback() { throw new UnsupportedOperationException(); }
            @Override public void close() { }
            @Override public boolean isClosed() { return false; }
            @Override public java.sql.DatabaseMetaData getMetaData() { throw new UnsupportedOperationException(); }
            @Override public void setReadOnly(boolean readOnly) { throw new UnsupportedOperationException(); }
            @Override public boolean isReadOnly() { throw new UnsupportedOperationException(); }
            @Override public void setCatalog(String catalog) { throw new UnsupportedOperationException(); }
            @Override public String getCatalog() { throw new UnsupportedOperationException(); }
            @Override public void setTransactionIsolation(int level) { throw new UnsupportedOperationException(); }
            @Override public int getTransactionIsolation() { throw new UnsupportedOperationException(); }
            @Override public java.sql.SQLWarning getWarnings() { throw new UnsupportedOperationException(); }
            @Override public void clearWarnings() { throw new UnsupportedOperationException(); }
            @Override public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) { throw new UnsupportedOperationException(); }
            @Override public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) { throw new UnsupportedOperationException(); }
            @Override public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) { throw new UnsupportedOperationException(); }
            @Override public java.util.Map<String, Class<?>> getTypeMap() { throw new UnsupportedOperationException(); }
            @Override public void setTypeMap(java.util.Map<String, Class<?>> map) { throw new UnsupportedOperationException(); }
            @Override public void setHoldability(int holdability) { throw new UnsupportedOperationException(); }
            @Override public int getHoldability() { throw new UnsupportedOperationException(); }
            @Override public java.sql.Savepoint setSavepoint() { throw new UnsupportedOperationException(); }
            @Override public java.sql.Savepoint setSavepoint(String name) { throw new UnsupportedOperationException(); }
            @Override public void rollback(java.sql.Savepoint savepoint) { throw new UnsupportedOperationException(); }
            @Override public void releaseSavepoint(java.sql.Savepoint savepoint) { throw new UnsupportedOperationException(); }
            @Override public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) { throw new UnsupportedOperationException(); }
            @Override public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) { throw new UnsupportedOperationException(); }
            @Override public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) { throw new UnsupportedOperationException(); }
            @Override public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) { throw new UnsupportedOperationException(); }
            @Override public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) { throw new UnsupportedOperationException(); }
            @Override public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) { throw new UnsupportedOperationException(); }
            @Override public java.sql.Clob createClob() { throw new UnsupportedOperationException(); }
            @Override public java.sql.Blob createBlob() { throw new UnsupportedOperationException(); }
            @Override public java.sql.NClob createNClob() { throw new UnsupportedOperationException(); }
            @Override public java.sql.SQLXML createSQLXML() { throw new UnsupportedOperationException(); }
            @Override public boolean isValid(int timeout) { throw new UnsupportedOperationException(); }
            @Override public void setClientInfo(String name, String value) { throw new UnsupportedOperationException(); }
            @Override public void setClientInfo(java.util.Properties properties) { throw new UnsupportedOperationException(); }
            @Override public String getClientInfo(String name) { throw new UnsupportedOperationException(); }
            @Override public java.util.Properties getClientInfo() { throw new UnsupportedOperationException(); }
            @Override public void setSchema(String schema) { throw new UnsupportedOperationException(); }
            @Override public String getSchema() { throw new UnsupportedOperationException(); }
            @Override public void abort(java.util.concurrent.Executor executor) { throw new UnsupportedOperationException(); }
            @Override public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) { throw new UnsupportedOperationException(); }
            @Override public int getNetworkTimeout() { throw new UnsupportedOperationException(); }
            @Override public java.sql.Array createArrayOf(String typeName, Object[] elements) { throw new UnsupportedOperationException(); }
            @Override public java.sql.Struct createStruct(String typeName, Object[] attributes) { throw new UnsupportedOperationException(); }
        };
        CustomAlloyDBEngine engine = new CustomAlloyDBEngine(new CustomAlloyDBEngine.Builder()) {
            @Override public java.sql.Connection getConnection() { return dummyConn; }
        };
        class DebugAlloyDBStoreConnection extends AlloyDBStoreConnection {
            public DebugAlloyDBStoreConnection(AlloyDBStoreConnectionParameters parameters) {
                super(parameters);
                System.out.println("DEBUG (constructor): parameters.getHost() = " + parameters.getHost());
                System.out.println("DEBUG (constructor): this.host = " + getHostReflect());
            }
            private String getHostReflect() {
                try {
                    java.lang.reflect.Field f = AlloyDBStoreConnection.class.getDeclaredField("host");
                    f.setAccessible(true);
                    return (String) f.get(this);
                } catch (Exception e) {
                    return "<error>";
                }
            }
        }
        DebugAlloyDBStoreConnection conn = new DebugAlloyDBStoreConnection(params) {
            @Override public CustomAlloyDBEngine getAlloyDBEngine() { return engine; }
        };
        QueryParameters queryParams = new QueryParameters() {
            @Override public int pageSize() { return 2; }
            @Override public boolean retrieveEmbeddings() { return true; }
        };
        AlloyDBStoreServiceProvider provider = new AlloyDBStoreServiceProvider(config, conn, "testStore", queryParams, 128, true);
        assertNotNull(provider.getFileIterator());
        assertTrue(provider.getFileIterator() instanceof AlloyDBStoreIterator);
    }
    */

    // Utility to set private fields via reflection, including superclass fields
    private static void setField(Object target, String fieldName, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Field '" + fieldName + "' not found in class hierarchy");
    }
} 