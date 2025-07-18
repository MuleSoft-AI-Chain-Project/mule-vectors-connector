package org.mule.extension.vectors.internal.connection.provider.store.aisearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.aisearch.AISearchStoreConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AISearchStoreConnectionProviderTest {

    AISearchStoreConnectionProvider provider;
    AISearchStoreConnectionParameters params;

    @BeforeEach
    void setUp() {
        provider = new AISearchStoreConnectionProvider();
        params = mock(AISearchStoreConnectionParameters.class);
        // Use reflection to set the private field
        try {
            java.lang.reflect.Field f = AISearchStoreConnectionProvider.class.getDeclaredField("aiSearchStoreConnectionParameters");
            f.setAccessible(true);
            f.set(provider, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void connect_success() throws Exception {
        // Mock static getHttpClient
        try (var mocked = mockStatic(org.apache.maven.wagon.shared.http.AbstractHttpClientWagon.class)) {
            mocked.when(org.apache.maven.wagon.shared.http.AbstractHttpClientWagon::getHttpClient).thenReturn(null);
            // Also mock AISearchStoreConnection constructor
            try (var connMocked = mockConstruction(AISearchStoreConnection.class, (mock, context) -> {
                // no-op
            })) {
                BaseStoreConnection conn = provider.connect();
                assertNotNull(conn);
            }
        }
    }

    @Test
    void connect_throwsException() {
        // Simulate AISearchStoreConnection constructor throws
        try (var mocked = mockStatic(org.apache.maven.wagon.shared.http.AbstractHttpClientWagon.class)) {
            mocked.when(org.apache.maven.wagon.shared.http.AbstractHttpClientWagon::getHttpClient).thenReturn(null);
            try (var connMocked = mockConstruction(AISearchStoreConnection.class, (mock, context) -> {
                throw new RuntimeException("fail");
            })) {
                ConnectionException ex = assertThrows(ConnectionException.class, provider::connect);
                assertTrue(ex.getMessage().contains("Failed to connect"));
            }
        }
    }
} 