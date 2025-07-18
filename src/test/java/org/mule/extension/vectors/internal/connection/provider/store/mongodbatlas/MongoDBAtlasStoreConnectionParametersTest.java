package org.mule.extension.vectors.internal.connection.provider.store.mongodbatlas;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.mongodbatlas.MongoDBAtlasStoreConnectionParameters;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class MongoDBAtlasStoreConnectionParametersTest {

    private MongoDBAtlasStoreConnectionParameters buildParams(String host, Integer port, String user, String password, String database, String options) throws Exception {
        MongoDBAtlasStoreConnectionParameters params = new MongoDBAtlasStoreConnectionParameters();
        setField(params, "host", host);
        setField(params, "port", port);
        setField(params, "user", user);
        setField(params, "password", password);
        setField(params, "database", database);
        setField(params, "options", options);
        return params;
    }

    private void setField(Object obj, String field, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(obj, value);
    }

    @Test
    void gettersReturnSetValues() throws Exception {
        MongoDBAtlasStoreConnectionParameters params = buildParams("host", 27017, "user", "pass", "db", "opt");
        assertEquals("host", params.getHost());
        assertEquals(27017, params.getPort());
        assertEquals("user", params.getUser());
        assertEquals("pass", params.getPassword());
        assertEquals("db", params.getDatabase());
        assertEquals("opt", params.getOptions());
    }

    @Test
    void gettersHandleNulls() throws Exception {
        MongoDBAtlasStoreConnectionParameters params = buildParams(null, null, null, null, null, null);
        assertNull(params.getHost());
        assertNull(params.getPort());
        assertNull(params.getUser());
        assertNull(params.getPassword());
        assertNull(params.getDatabase());
        assertNull(params.getOptions());
    }

    @Test
    void gettersHandleEmptyStrings() throws Exception {
        MongoDBAtlasStoreConnectionParameters params = buildParams("", null, "", "", "", "");
        assertEquals("", params.getHost());
        assertNull(params.getPort());
        assertEquals("", params.getUser());
        assertEquals("", params.getPassword());
        assertEquals("", params.getDatabase());
        assertEquals("", params.getOptions());
    }

    @Test
    void defaultValuesAreNull() {
        MongoDBAtlasStoreConnectionParameters params = new MongoDBAtlasStoreConnectionParameters();
        assertNull(params.getHost());
        assertNull(params.getPort());
        assertNull(params.getUser());
        assertNull(params.getPassword());
        assertNull(params.getDatabase());
        assertNull(params.getOptions());
    }
} 
