package org.mule.extension.vectors.internal.connection.provider.store.elasticsearch;

import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.elasticsearch.ElasticsearchStoreConnectionParameters;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElasticsearchStoreConnectionTest {

    @Mock
    ElasticsearchStoreConnectionParameters parameters;
    @Mock
    RestClient mockRestClient;

    ElasticsearchStoreConnection connection;

    @BeforeEach
    void setUp() {
        when(parameters.getUrl()).thenReturn("http://localhost:9200");
        when(parameters.getUser()).thenReturn("elastic");
        when(parameters.getPassword()).thenReturn("pass");
        when(parameters.getApiKey()).thenReturn("key");
        connection = new ElasticsearchStoreConnection(parameters);
    }

    private void injectRestClient(RestClient restClient) throws Exception {
        Field f = connection.getClass().getDeclaredField("restClient");
        f.setAccessible(true);
        f.set(connection, restClient);
    }

    @Test
    void gettersReturnSetValues() {
        assertEquals("http://localhost:9200", connection.getUrl());
        assertEquals("elastic", connection.getUser());
        assertEquals("pass", connection.getPassword());
        assertEquals("key", connection.getApiKey());
        assertEquals(parameters, connection.getConnectionParameters());
    }

    @Test
    void getVectorStoreReturnsConstant() {
        assertEquals("ELASTICSEARCH", connection.getVectorStore());
    }

    @Test
    void disconnectClosesRestClient() throws Exception {
        injectRestClient(mockRestClient);
        doNothing().when(mockRestClient).close();
        assertDoesNotThrow(() -> connection.disconnect());
        verify(mockRestClient, times(1)).close();
    }

    @Test
    void disconnectThrowsRuntimeExceptionOnCloseError() throws Exception {
        injectRestClient(mockRestClient);
        doThrow(new IOException("fail")).when(mockRestClient).close();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> connection.disconnect());
        assertEquals("fail", ex.getCause().getMessage());
    }

    @Test
    void validateThrowsIfUrlMissing() {
        when(parameters.getUrl()).thenReturn(null);
        ModuleException ex = assertThrows(ModuleException.class, () -> connection.validate());
        assertTrue(ex.getMessage().contains("URL is required"));
    }

    @Test
    void validateThrowsIfPasswordAndApiKeyMissing() {
        when(parameters.getPassword()).thenReturn(null);
        when(parameters.getApiKey()).thenReturn(null);
        ModuleException ex = assertThrows(ModuleException.class, () -> connection.validate());
        assertTrue(ex.getMessage().contains("Either password or API Key is required"));
    }

    @Test
    void validateThrowsIfRestClientFails() throws Exception {
        injectRestClient(mockRestClient);
        when(mockRestClient.getNodes()).thenThrow(new RuntimeException("fail connect"));
        // Only stub password, as apiKey is not used in this path
        when(parameters.getPassword()).thenReturn("pass");
        when(parameters.getUrl()).thenReturn("http://localhost:9200");
        ModuleException ex = assertThrows(ModuleException.class, () -> connection.validate());
        assertTrue(ex.getMessage().contains("Failed to connect"));
    }

    @Test
    void validateSucceedsIfRestClientWorks() throws Exception {
        injectRestClient(mockRestClient);
        when(mockRestClient.getNodes()).thenReturn(null);
        when(parameters.getPassword()).thenReturn("pass");
        assertDoesNotThrow(() -> connection.validate());
    }

    @Test
    void initialiseWithUserPassword() throws Exception {
        // Use lenient() to avoid UnnecessaryStubbingException
        lenient().when(parameters.getUser()).thenReturn("elastic");
        lenient().when(parameters.getPassword()).thenReturn("pass");
        assertThrows(IOException.class, () -> connection.initialise());
    }

    @Test
    void initialiseWithApiKey() throws Exception {
        // Use lenient() to avoid UnnecessaryStubbingException
        lenient().when(parameters.getUser()).thenReturn("");
        lenient().when(parameters.getApiKey()).thenReturn("key");
        assertThrows(IOException.class, () -> connection.initialise());
    }
} 
