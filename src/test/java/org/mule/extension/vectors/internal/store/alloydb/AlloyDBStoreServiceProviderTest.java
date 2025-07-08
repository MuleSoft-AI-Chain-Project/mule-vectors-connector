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
