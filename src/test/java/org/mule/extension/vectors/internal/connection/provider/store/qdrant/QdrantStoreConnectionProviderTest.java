package org.mule.extension.vectors.internal.connection.provider.store.qdrant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.BaseStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.provider.store.qdrant.QdrantStoreConnectionProvider;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.MockedConstruction;

class QdrantStoreConnectionProviderTest {

    QdrantStoreConnectionProvider provider;
    QdrantStoreConnectionParameters params;

    @BeforeEach
    void setUp() {
        provider = new QdrantStoreConnectionProvider();
        params = new QdrantStoreConnectionParameters();
        // Set minimal valid values via reflection
        try {
            java.lang.reflect.Field hostF = QdrantStoreConnectionParameters.class.getDeclaredField("host");
            hostF.setAccessible(true);
            hostF.set(params, "localhost");
            java.lang.reflect.Field portF = QdrantStoreConnectionParameters.class.getDeclaredField("gprcPort");
            portF.setAccessible(true);
            portF.set(params, 6334);
            java.lang.reflect.Field tlsF = QdrantStoreConnectionParameters.class.getDeclaredField("useTLS");
            tlsF.setAccessible(true);
            tlsF.set(params, false);
            java.lang.reflect.Field keyF = QdrantStoreConnectionParameters.class.getDeclaredField("textSegmentKey");
            keyF.setAccessible(true);
            keyF.set(params, "text-segment");
            java.lang.reflect.Field apiF = QdrantStoreConnectionParameters.class.getDeclaredField("apiKey");
            apiF.setAccessible(true);
            apiF.set(params, "apikey");
        } catch (Exception e) {
            fail(e);
        }
        provider.setQdrantStoreConnectionParameters(params);
    }

    @Test
    void connect_returnsConnection() throws Exception {
        try (MockedConstruction<QdrantStoreConnection> mocked = org.mockito.Mockito.mockConstruction(QdrantStoreConnection.class, (mock, context) -> {})) {
            provider.initialise();
            BaseStoreConnection conn = provider.connect();
            assertNotNull(conn);
            assertTrue(conn instanceof QdrantStoreConnection);
        }
    }

    @Test
    void dispose_disconnects() throws Exception {
        try (MockedConstruction<QdrantStoreConnection> mocked = org.mockito.Mockito.mockConstruction(QdrantStoreConnection.class, (mock, context) -> {
            org.mockito.Mockito.doNothing().when(mock).disconnect();
        })) {
            provider.initialise();
            assertDoesNotThrow(provider::dispose);
        }
    }

    @Test
    void initialise_handlesConnectionException() {
        QdrantStoreConnectionProvider prov = new QdrantStoreConnectionProvider() {
            @Override
            public void initialise() throws org.mule.runtime.api.lifecycle.InitialisationException {
                throw new org.mule.runtime.api.lifecycle.InitialisationException(new Exception("fail"), this);
            }
        };
        assertThrows(org.mule.runtime.api.lifecycle.InitialisationException.class, prov::initialise);
    }
} 