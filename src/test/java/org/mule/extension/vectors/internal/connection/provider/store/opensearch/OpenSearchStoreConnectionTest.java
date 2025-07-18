package org.mule.extension.vectors.internal.connection.provider.store.opensearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.opensearch.OpenSearchStoreConnectionParameters;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.opensearch.client.opensearch.OpenSearchClient;

import static org.junit.jupiter.api.Assertions.*;

class OpenSearchStoreConnectionTest {
    OpenSearchStoreConnectionParameters params;
    OpenSearchStoreConnection conn;

    @BeforeEach
    void setup() {
        params = new OpenSearchStoreConnectionParameters();
        setField(params, "url", "http://localhost:9200");
        setField(params, "user", "user");
        setField(params, "password", "pass");
        setField(params, "apiKey", "key");
        conn = new OpenSearchStoreConnection(params);
    }

    @Test
    void getters_returnValues() {
        assertEquals("http://localhost:9200", conn.getUrl());
        assertEquals("user", conn.getUser());
        assertEquals("pass", conn.getPassword());
        assertEquals("key", conn.getApiKey());
        assertEquals(params, conn.getConnectionParameters());
    }

    @Test
    void getVectorStore_returnsConstant() {
        assertEquals("OPENSEARCH", conn.getVectorStore());
    }

    @Test
    void initialise_doesNotThrow() {
        assertDoesNotThrow(() -> conn.initialise());
    }

    @Test
    void disconnect_doesNotThrow() {
        assertDoesNotThrow(() -> conn.disconnect());
    }

    @Test
    void validate_throwsIfUrlMissing() {
        setField(params, "url", null);
        OpenSearchStoreConnection badConn = new OpenSearchStoreConnection(params);
        Exception ex = assertThrows(ModuleException.class, badConn::validate);
        assertTrue(ex.getMessage().contains("URL is required"));
    }

    @Test
    void validate_throwsIfPasswordAndApiKeyMissing() {
        setField(params, "password", null);
        setField(params, "apiKey", null);
        OpenSearchStoreConnection badConn = new OpenSearchStoreConnection(params);
        setField(badConn, "openSearchClient", new OpenSearchClient(null));
        Exception ex = assertThrows(ModuleException.class, badConn::validate);
        assertTrue(ex.getMessage().contains("Either password or API Key is required"));
    }

    @Test
    void validate_throwsIfPingFails() {
        setField(params, "url", "http://localhost:9200");
        setField(params, "password", "pass");
        setField(params, "apiKey", "key");
        OpenSearchStoreConnection badConn = new OpenSearchStoreConnection(params);
        setField(badConn, "openSearchClient", new OpenSearchClient(null));
        Exception ex = assertThrows(ModuleException.class, badConn::validate);
        assertTrue(ex.getMessage().contains("Failed to validate connection"));
    }

    // Reflection helper
    static void setField(Object obj, String field, Object value) {
        try {
            Class<?> c = obj.getClass();
            while (c != null) {
                try {
                    java.lang.reflect.Field f = c.getDeclaredField(field);
                    f.setAccessible(true);
                    f.set(obj, value);
                    return;
                } catch (NoSuchFieldException e) {
                    c = c.getSuperclass();
                }
            }
            throw new RuntimeException("Field not found: " + field);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 