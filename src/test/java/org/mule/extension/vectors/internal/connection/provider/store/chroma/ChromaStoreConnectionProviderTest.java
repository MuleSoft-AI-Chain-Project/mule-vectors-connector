package org.mule.extension.vectors.internal.connection.provider.store.chroma;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChromaStoreConnectionProviderTest {

    ChromaStoreConnectionProvider provider;
    ChromaStoreConnectionParameters params;

    @BeforeEach
    void setUp() {
        provider = new ChromaStoreConnectionProvider();
        params = mock(ChromaStoreConnectionParameters.class);
        // Use reflection to set the private field
        try {
            java.lang.reflect.Field f = ChromaStoreConnectionProvider.class.getDeclaredField("chromaStoreConnectionParameters");
            f.setAccessible(true);
            f.set(provider, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void connect_success() throws Exception {
        // Mock getHttpClient
        try (var providerMocked = mockConstruction(ChromaStoreConnection.class, (mock, context) -> {})) {
            BaseStoreConnection conn = provider.connect();
            assertNotNull(conn);
        }
    }

    @Test
    void connect_throwsException() {
        try (var providerMocked = mockConstruction(ChromaStoreConnection.class, (mock, context) -> {
            throw new RuntimeException("fail");
        })) {
            ConnectionException ex = assertThrows(ConnectionException.class, provider::connect);
            assertTrue(ex.getMessage().contains("Failed to connect"));
        }
    }
} 