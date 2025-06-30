package org.mule.extension.vectors.internal.store.elasticsearch;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.store.elasticsearch.ElasticsearchStoreConnectionParameters;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class ElasticsearchStoreConnectionParametersTest {

    private ElasticsearchStoreConnectionParameters buildParams(String url, String user, String password, String apiKey) throws Exception {
        ElasticsearchStoreConnectionParameters params = new ElasticsearchStoreConnectionParameters();
        setField(params, "url", url);
        setField(params, "user", user);
        setField(params, "password", password);
        setField(params, "apiKey", apiKey);
        return params;
    }

    private void setField(Object obj, String field, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(obj, value);
    }

    @Test
    void gettersReturnSetValues() throws Exception {
        ElasticsearchStoreConnectionParameters params = buildParams("http://localhost:9200", "elastic", "pass", "key");
        assertEquals("http://localhost:9200", params.getUrl());
        assertEquals("elastic", params.getUser());
        assertEquals("pass", params.getPassword());
        assertEquals("key", params.getApiKey());
    }

    @Test
    void gettersReturnNullWhenUnset() throws Exception {
        ElasticsearchStoreConnectionParameters params = buildParams(null, null, null, null);
        assertNull(params.getUrl());
        assertNull(params.getUser());
        assertNull(params.getPassword());
        assertNull(params.getApiKey());
    }

    @Test
    void gettersReturnEmptyString() throws Exception {
        ElasticsearchStoreConnectionParameters params = buildParams("", "", "", "");
        assertEquals("", params.getUrl());
        assertEquals("", params.getUser());
        assertEquals("", params.getPassword());
        assertEquals("", params.getApiKey());
    }
} 