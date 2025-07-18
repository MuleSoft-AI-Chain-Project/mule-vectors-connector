package org.mule.extension.vectors.internal.connection.provider.store.ephemeralfile;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.ephemeralfile.EphemeralFileStoreConnectionParameters;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EphemeralFileStoreConnectionParametersTest {

    private EphemeralFileStoreConnectionParameters buildParams(String workingDir) throws Exception {
        EphemeralFileStoreConnectionParameters params = new EphemeralFileStoreConnectionParameters();
        Field f = params.getClass().getDeclaredField("workingDir");
        f.setAccessible(true);
        f.set(params, workingDir);
        return params;
    }

    @Test
    void getterReturnsSetValue() throws Exception {
        EphemeralFileStoreConnectionParameters params = buildParams("/tmp/ephemeral");
        assertEquals("/tmp/ephemeral", params.getWorkingDir());
    }

    @Test
    void getterHandlesNull() throws Exception {
        EphemeralFileStoreConnectionParameters params = buildParams(null);
        assertNull(params.getWorkingDir());
    }

    @Test
    void getterHandlesEmptyString() throws Exception {
        EphemeralFileStoreConnectionParameters params = buildParams("");
        assertEquals("", params.getWorkingDir());
    }

    @Test
    void defaultValueIsNull() {
        EphemeralFileStoreConnectionParameters params = new EphemeralFileStoreConnectionParameters();
        assertNull(params.getWorkingDir());
    }
} 
