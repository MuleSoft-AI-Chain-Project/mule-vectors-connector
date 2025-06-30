package org.mule.extension.vectors.internal.store.weaviate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnection;
import org.mule.extension.vectors.internal.connection.store.weaviate.WeaviateStoreConnectionParameters;
import org.mule.runtime.http.api.client.HttpClient;
import org.mule.runtime.extension.api.exception.ModuleException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeaviateStoreConnectionTest {

    @Mock
    WeaviateStoreConnectionParameters parameters;
    @Mock
    HttpClient mockHttpClient;

    WeaviateStoreConnection connection;

    @BeforeEach
    void setUp() {
        when(parameters.getScheme()).thenReturn("https");
        when(parameters.getHost()).thenReturn("localhost");
        when(parameters.getPort()).thenReturn(8080);
        when(parameters.getApiKey()).thenReturn("key");
        when(parameters.getConsistencyLevel()).thenReturn("ALL");
        when(parameters.isAvoidDups()).thenReturn(true);
        when(parameters.isSecuredGrpc()).thenReturn(false);
        when(parameters.getGrpcPort()).thenReturn(null);
        when(parameters.isUseGrpcForInserts()).thenReturn(false);
        connection = new WeaviateStoreConnection(parameters, mockHttpClient);
    }

    @Test
    void constructorSetsParameters() {
        assertEquals(parameters, connection.getConnectionParameters());
    }

    @Test
    void getVectorStoreReturnsConstant() {
        assertEquals("WEAVIATE", connection.getVectorStore());
    }

    @Test
    void disconnectDoesNothing() {
        assertDoesNotThrow(() -> connection.disconnect());
    }

    @Test
    void validateThrowsIfMissingScheme() {
        when(parameters.getScheme()).thenReturn(null);
        ModuleException ex = assertThrows(ModuleException.class, () -> connection.validate());
        assertTrue(ex.getMessage().toLowerCase().contains("scheme"));
    }

    @Test
    void validateThrowsIfMissingHost() {
        when(parameters.getHost()).thenReturn(null);
        ModuleException ex = assertThrows(ModuleException.class, () -> connection.validate());
        assertTrue(ex.getMessage().toLowerCase().contains("host"));
    }

    @Test
    void validateThrowsIfMissingApiKey() {
        when(parameters.getApiKey()).thenReturn(null);
        ModuleException ex = assertThrows(ModuleException.class, () -> connection.validate());
        assertTrue(ex.getMessage().toLowerCase().contains("api key"));
    }
} 