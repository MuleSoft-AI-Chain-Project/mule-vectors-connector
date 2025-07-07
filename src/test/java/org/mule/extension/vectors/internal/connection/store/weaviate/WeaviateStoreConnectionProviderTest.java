package org.mule.extension.vectors.internal.connection.store.weaviate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.http.api.client.HttpClient;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeaviateStoreConnectionProviderTest {

    WeaviateStoreConnectionProvider provider;
    WeaviateStoreConnectionParameters mockParams;
    HttpClient mockHttpClient;

    @BeforeEach
    void setUp() throws Exception {
        provider = new WeaviateStoreConnectionProvider();
        mockParams = mock(WeaviateStoreConnectionParameters.class);
        mockHttpClient = mock(HttpClient.class);
        // Inject mockParams
        Field f = provider.getClass().getDeclaredField("weaviateStoreConnectionParameters");
        f.setAccessible(true);
        f.set(provider, mockParams);
        // Inject mockHttpClient (via supertype)
        Field httpField = provider.getClass().getSuperclass().getDeclaredField("httpClient");
        httpField.setAccessible(true);
        httpField.set(provider, mockHttpClient);
    }

    @Test
    void connectReturnsWeaviateStoreConnection() throws Exception {
        WeaviateStoreConnection conn = (WeaviateStoreConnection) provider.connect();
        assertNotNull(conn);
        assertEquals(mockParams, conn.getConnectionParameters());
        assertEquals(mockHttpClient, conn.getHttpClient());
    }

    @Test
    void connectWrapsExceptionAsConnectionException() throws Exception {
        // Simulate error in constructor
        Field f = provider.getClass().getDeclaredField("weaviateStoreConnectionParameters");
        f.setAccessible(true);
        f.set(provider, null); // Will cause NPE
        ConnectionException ex = assertThrows(ConnectionException.class, () -> provider.connect());
        assertTrue(ex.getMessage().toLowerCase().contains("failed to connect"));
    }
} 
