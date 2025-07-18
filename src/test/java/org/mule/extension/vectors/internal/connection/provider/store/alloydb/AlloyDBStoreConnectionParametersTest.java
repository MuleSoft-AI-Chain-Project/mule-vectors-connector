package org.mule.extension.vectors.internal.connection.store.alloydb;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.alloydb.AlloyDBStoreConnectionParameters;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AlloyDBStoreConnectionParametersTest {

    private AlloyDBStoreConnectionParameters buildParams(String projectId, String region, String cluster, String instance, String iamAccountEmail, String host, String ipType, int port, String database, String user, String password) {
        AlloyDBStoreConnectionParameters params = new AlloyDBStoreConnectionParameters();
        try {
            setField(params, "projectId", projectId);
            setField(params, "region", region);
            setField(params, "cluster", cluster);
            setField(params, "instance", instance);
            setField(params, "iamAccountEmail", iamAccountEmail);
            setField(params, "host", host);
            setField(params, "ipType", ipType);
            setField(params, "port", port);
            setField(params, "database", database);
            setField(params, "user", user);
            setField(params, "password", password);
        } catch (Exception e) {
            fail("Reflection setup failed: " + e.getMessage());
        }
        return params;
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    @Test
    void shouldReturnAllSetValues() {
        AlloyDBStoreConnectionParameters params = buildParams(
                "pid", "reg", "clu", "inst", "email", "host", "private", 1234, "db", "user", "pass"
        );
        assertEquals("pid", params.getProjectId());
        assertEquals("reg", params.getRegion());
        assertEquals("clu", params.getCluster());
        assertEquals("inst", params.getInstance());
        assertEquals("email", params.getIamAccountEmail());
        assertEquals("host", params.getHost());
        assertEquals("private", params.getIpType());
        assertEquals(1234, params.getPort());
        assertEquals("db", params.getDatabase());
        assertEquals("user", params.getUser());
        assertEquals("pass", params.getPassword());
    }

    @Test
    void shouldReturnNullForUnsetStrings() {
        AlloyDBStoreConnectionParameters params = buildParams(null, null, null, null, null, null, null, 0, null, null, null);
        assertNull(params.getProjectId());
        assertNull(params.getRegion());
        assertNull(params.getCluster());
        assertNull(params.getInstance());
        assertNull(params.getIamAccountEmail());
        assertNull(params.getHost());
        assertNull(params.getIpType());
        assertEquals(0, params.getPort());
        assertNull(params.getDatabase());
        assertNull(params.getUser());
        assertNull(params.getPassword());
    }

    @Test
    void shouldReturnDefaultValuesForOptionalFields() {
        AlloyDBStoreConnectionParameters params = new AlloyDBStoreConnectionParameters();
        // ipType and port are @Optional with defaultValue
        assertNull(params.getIpType(), "Default ipType is not set by constructor");
        assertEquals(0, params.getPort(), "Default port is not set by constructor");
        // Note: Mule will inject default values at runtime, not in plain Java
    }

    @Test
    void shouldHandleEmptyStrings() {
        AlloyDBStoreConnectionParameters params = buildParams("", "", "", "", "", "", "", 0, "", "", "");
        assertEquals("", params.getProjectId());
        assertEquals("", params.getRegion());
        assertEquals("", params.getCluster());
        assertEquals("", params.getInstance());
        assertEquals("", params.getIamAccountEmail());
        assertEquals("", params.getHost());
        assertEquals("", params.getIpType());
        assertEquals(0, params.getPort());
        assertEquals("", params.getDatabase());
        assertEquals("", params.getUser());
        assertEquals("", params.getPassword());
    }
} 
