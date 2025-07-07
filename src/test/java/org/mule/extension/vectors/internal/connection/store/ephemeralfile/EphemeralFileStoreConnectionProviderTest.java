package org.mule.extension.vectors.internal.connection.store.ephemeralfile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnectionProvider;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnectionParameters;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lifecycle.InitialisationException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EphemeralFileStoreConnectionProviderTest {

    EphemeralFileStoreConnectionProvider provider;
    EphemeralFileStoreConnectionParameters mockParams;

    @BeforeEach
    void setUp() throws Exception {
        provider = new EphemeralFileStoreConnectionProvider();
        mockParams = mock(EphemeralFileStoreConnectionParameters.class);
        Field f = provider.getClass().getDeclaredField("ephemeralFileStoreConnectionParameters");
        f.setAccessible(true);
        f.set(provider, mockParams);
    }

    @Test
    void connectShouldReturnConnection() throws ConnectionException {
        when(mockParams.getWorkingDir()).thenReturn("/tmp/ephemeral");
        assertTrue(provider.connect() instanceof EphemeralFileStoreConnection);
    }

    @Test
    void connectShouldThrowOnException() {
        // Simulate exception in connection constructor by making getWorkingDir() throw
        when(mockParams.getWorkingDir()).thenThrow(new RuntimeException("fail"));
        assertThrows(ConnectionException.class, () -> provider.connect());
    }

    @Test
    void disposeShouldNotThrow() {
        assertDoesNotThrow(() -> provider.dispose());
    }

    @Test
    void initialiseShouldNotThrow() {
        assertDoesNotThrow(() -> provider.initialise());
    }
} 
