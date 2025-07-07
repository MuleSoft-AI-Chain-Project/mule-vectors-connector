package org.mule.extension.vectors.internal.connection.store.pinecone;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.store.pinecone.PineconeStoreConnectionParameters;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class PineconeStoreConnectionParametersTest {

    private PineconeStoreConnectionParameters buildParams(String cloud, String region, String apiKey) throws Exception {
        PineconeStoreConnectionParameters params = new PineconeStoreConnectionParameters();
        setField(params, "cloud", cloud);
        setField(params, "region", region);
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
        PineconeStoreConnectionParameters params = buildParams("AWS", "us-east-1", "key");
        assertEquals("AWS", params.getCloud());
        assertEquals("us-east-1", params.getRegion());
        assertEquals("key", params.getApiKey());
    }

    @Test
    void gettersHandleNulls() throws Exception {
        PineconeStoreConnectionParameters params = buildParams(null, null, null);
        assertNull(params.getCloud());
        assertNull(params.getRegion());
        assertNull(params.getApiKey());
    }

    @Test
    void gettersHandleEmptyStrings() throws Exception {
        PineconeStoreConnectionParameters params = buildParams("", "", "");
        assertEquals("", params.getCloud());
        assertEquals("", params.getRegion());
        assertEquals("", params.getApiKey());
    }

    @Test
    void defaultValuesForOptionalFields() throws Exception {
        PineconeStoreConnectionParameters params = new PineconeStoreConnectionParameters();
        // All fields default to null
        assertNull(params.getCloud());
        assertNull(params.getRegion());
        assertNull(params.getApiKey());
    }
} 
