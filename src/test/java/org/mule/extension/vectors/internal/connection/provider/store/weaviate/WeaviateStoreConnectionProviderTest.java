package org.mule.extension.vectors.internal.connection.provider.store.weaviate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.weaviate.WeaviateStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.weaviate.WeaviateStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.weaviate.WeaviateStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeaviateStoreConnectionProviderTest {

    WeaviateStoreConnectionProvider provider;
    WeaviateStoreConnectionParameters params;

    @BeforeEach
    void setUp() {
        provider = new WeaviateStoreConnectionProvider();
        params = mock(WeaviateStoreConnectionParameters.class);
        // Use reflection to set the private field
        try {
            java.lang.reflect.Field f = WeaviateStoreConnectionProvider.class.getDeclaredField("weaviateStoreConnectionParameters");
            f.setAccessible(true);
            f.set(provider, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void connect_success() throws Exception {
        try (var providerMocked = mockConstruction(WeaviateStoreConnection.class, (mock, context) -> {})) {
            BaseStoreConnection conn = provider.connect();
            assertNotNull(conn);
        }
    }

    @Test
    void connect_throwsException() {
        try (var providerMocked = mockConstruction(WeaviateStoreConnection.class, (mock, context) -> {
            throw new RuntimeException("fail");
        })) {
            ConnectionException ex = assertThrows(ConnectionException.class, provider::connect);
            assertTrue(ex.getMessage().contains("Failed to connect"));
        }
    }
} 
