package org.mule.extension.vectors.internal.connection.store.mongodbatlas;

import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.provider.store.mongodbatlas.MongoDBAtlasStoreConnection;
import org.mule.extension.vectors.internal.connection.provider.store.mongodbatlas.MongoDBAtlasStoreConnectionParameters;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoDBAtlasStoreConnectionTest {

    @Mock
    MongoDBAtlasStoreConnectionParameters parameters;
    @Mock
    MongoClient mockClient;
    MongoDBAtlasStoreConnection connection;

    @BeforeEach
    void setUp() {
        when(parameters.getHost()).thenReturn("host");
        when(parameters.getPort()).thenReturn(27017);
        when(parameters.getUser()).thenReturn("user");
        when(parameters.getPassword()).thenReturn("pass");
        when(parameters.getDatabase()).thenReturn("db");
        when(parameters.getOptions()).thenReturn("opt");
        connection = new MongoDBAtlasStoreConnection(parameters);
    }

    @Test
    void shouldReturnFieldsFromConstructor() {
        assertEquals("db", connection.getDatabase());
        assertEquals(parameters, connection.getConnectionParameters());
    }

    @Test
    void shouldReturnVectorStoreConstant() {
        assertEquals(Constants.VECTOR_STORE_MONGODB_ATLAS, connection.getVectorStore());
    }

    @Test
    void disconnectShouldCloseClientIfNotNull() throws Exception {
        setField(connection, "mongoClient", mockClient);
        connection.disconnect();
        verify(mockClient, times(1)).close();
    }

    @Test
    void disconnectShouldNotThrowIfClientNull() {
        assertDoesNotThrow(() -> connection.disconnect());
    }

    @Test
    void validateShouldSucceed() throws Exception {
        setField(connection, "mongoClient", mockClient);
        when(mockClient.listDatabaseNames()).thenReturn(mock(com.mongodb.client.MongoIterable.class));
        when(mockClient.listDatabaseNames().first()).thenReturn("db");
        assertDoesNotThrow(() -> connection.validate());
    }

    @Test
    void validateShouldThrowIfHostNullOrBlank() throws Exception {
        when(parameters.getHost()).thenReturn(null);
        MongoDBAtlasStoreConnection conn = new MongoDBAtlasStoreConnection(parameters);
        setField(conn, "mongoClient", mockClient);
        assertThrows(ModuleException.class, conn::validate);
        when(parameters.getHost()).thenReturn("");
        conn = new MongoDBAtlasStoreConnection(parameters);
        setField(conn, "mongoClient", mockClient);
        assertThrows(ModuleException.class, conn::validate);
    }

    @Test
    void validateShouldThrowIfUserNullOrBlank() throws Exception {
        when(parameters.getUser()).thenReturn(null);
        MongoDBAtlasStoreConnection conn = new MongoDBAtlasStoreConnection(parameters);
        setField(conn, "mongoClient", mockClient);
        assertThrows(ModuleException.class, conn::validate);
        when(parameters.getUser()).thenReturn("");
        conn = new MongoDBAtlasStoreConnection(parameters);
        setField(conn, "mongoClient", mockClient);
        assertThrows(ModuleException.class, conn::validate);
    }

    @Test
    void validateShouldThrowIfPasswordNullOrBlank() throws Exception {
        when(parameters.getPassword()).thenReturn(null);
        MongoDBAtlasStoreConnection conn = new MongoDBAtlasStoreConnection(parameters);
        setField(conn, "mongoClient", mockClient);
        assertThrows(ModuleException.class, conn::validate);
        when(parameters.getPassword()).thenReturn("");
        conn = new MongoDBAtlasStoreConnection(parameters);
        setField(conn, "mongoClient", mockClient);
        assertThrows(ModuleException.class, conn::validate);
    }

    @Test
    void validateShouldThrowIfDatabaseNullOrBlank() throws Exception {
        when(parameters.getDatabase()).thenReturn(null);
        MongoDBAtlasStoreConnection conn = new MongoDBAtlasStoreConnection(parameters);
        setField(conn, "mongoClient", mockClient);
        assertThrows(ModuleException.class, conn::validate);
        when(parameters.getDatabase()).thenReturn("");
        conn = new MongoDBAtlasStoreConnection(parameters);
        setField(conn, "mongoClient", mockClient);
        assertThrows(ModuleException.class, conn::validate);
    }

    @Test
    void validateShouldThrowIfClientFails() throws Exception {
        setField(connection, "mongoClient", mockClient);
        when(mockClient.listDatabaseNames()).thenThrow(new RuntimeException("fail"));
        assertThrows(ModuleException.class, () -> connection.validate());
    }

    private void setField(Object obj, String field, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(obj, value);
    }
} 
