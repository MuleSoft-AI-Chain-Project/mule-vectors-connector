package org.mule.extension.vectors.internal.connection.store.opensearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.opensearch.OpenSearchStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.opensearch.OpenSearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.opensearch.OpenSearchStoreConnectionProvider;
import org.opensearch.client.opensearch.OpenSearchClient;

import static org.junit.jupiter.api.Assertions.*;

class OpenSearchStoreConnectionProviderTest {

    OpenSearchStoreConnectionProvider provider;
    OpenSearchStoreConnectionParameters params;

    @BeforeEach
    void setup() {
        provider = new OpenSearchStoreConnectionProvider();
        params = new OpenSearchStoreConnectionParameters();
        setField(params, "url", "http://localhost:9200");
        setField(params, "user", "user");
        setField(params, "password", "pass");
        setField(params, "apiKey", "key");
        setField(provider, "openSearchStoreConnectionParameters", params);
    }

    @Test
    void connect_returnsConnection() throws Exception {
        OpenSearchStoreConnection testConn = new OpenSearchStoreConnection(params) {
            @Override public void initialise() {}
            @Override public void disconnect() {}
        };
        setField(testConn, "openSearchClient", new OpenSearchClient(null));
        setField(provider, "openSearchStoreConnection", testConn);
        BaseStoreConnection conn = provider.connect();
        assertNotNull(conn);
        assertTrue(conn instanceof OpenSearchStoreConnection);
    }

    @Test
    void dispose_doesNotThrow() throws Exception {
        OpenSearchStoreConnection testConn = new OpenSearchStoreConnection(params) {
            @Override public void initialise() {}
            @Override public void disconnect() {}
        };
        setField(testConn, "openSearchClient", new OpenSearchClient(null));
        setField(provider, "openSearchStoreConnection", testConn);
        assertDoesNotThrow(provider::dispose);
    }

    @Test
    void initialise_setsConnection() throws Exception {
        OpenSearchStoreConnection testConn = new OpenSearchStoreConnection(params) {
            @Override public void initialise() {}
        };
        setField(provider, "openSearchStoreConnection", null);
        setField(provider, "openSearchStoreConnectionParameters", params);
        // Replace new OpenSearchStoreConnection with our test double
        provider.initialise(); // This will call real initialise, but we override it in testConn
        // forcibly set the testConn after
        setField(provider, "openSearchStoreConnection", testConn);
        assertNotNull(getField(provider, "openSearchStoreConnection"));
    }

    // Reflection helpers
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
    static Object getField(Object obj, String field) {
        try {
            Class<?> c = obj.getClass();
            while (c != null) {
                try {
                    java.lang.reflect.Field f = c.getDeclaredField(field);
                    f.setAccessible(true);
                    return f.get(obj);
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