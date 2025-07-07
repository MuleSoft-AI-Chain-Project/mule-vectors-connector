package org.mule.extension.vectors.internal.connection.store.ephemeralfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnectionParameters;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.extension.api.exception.ModuleException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EphemeralFileStoreConnectionTest {

    @Mock
    EphemeralFileStoreConnectionParameters parameters;
    EphemeralFileStoreConnection connection;

    @BeforeEach
    void setUp() {
        when(parameters.getWorkingDir()).thenReturn("/tmp/ephemeral");
        connection = new EphemeralFileStoreConnection(parameters);
    }

    @Test
    void shouldReturnFieldsFromConstructor() {
        assertEquals("/tmp/ephemeral", connection.getWorkingDir());
        assertEquals(parameters, connection.getConnectionParameters());
    }

    @Test
    void shouldReturnVectorStoreConstant() {
        assertEquals(Constants.VECTOR_STORE_EPHEMERAL_FILE, connection.getVectorStore());
    }

    @Test
    void disconnectShouldNotThrow() {
        assertDoesNotThrow(() -> connection.disconnect());
    }

    @Test
    void validateShouldSucceed() {
        when(parameters.getWorkingDir()).thenReturn("/tmp/ephemeral");
        assertDoesNotThrow(() -> connection.validate());
    }

    @Test
    void validateShouldThrowIfWorkingDirNull() {
        when(parameters.getWorkingDir()).thenReturn(null);
        EphemeralFileStoreConnection conn = new EphemeralFileStoreConnection(parameters);
        assertThrows(IllegalArgumentException.class, conn::validate);
    }

    @Test
    void validateShouldThrowIfWorkingDirBlank() {
        when(parameters.getWorkingDir()).thenReturn("");
        EphemeralFileStoreConnection conn = new EphemeralFileStoreConnection(parameters);
        assertThrows(IllegalArgumentException.class, conn::validate);
        when(parameters.getWorkingDir()).thenReturn("   ");
        conn = new EphemeralFileStoreConnection(parameters);
        assertThrows(IllegalArgumentException.class, conn::validate);
    }
} 
