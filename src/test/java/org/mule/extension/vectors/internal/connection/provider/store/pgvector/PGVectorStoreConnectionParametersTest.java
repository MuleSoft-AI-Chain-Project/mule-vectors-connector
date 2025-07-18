package org.mule.extension.vectors.internal.connection.provider.store.pgvector;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.pgvector.PGVectorStoreConnectionParameters;

import static org.junit.jupiter.api.Assertions.*;

class PGVectorStoreConnectionParametersTest {

    @Test
    void getters_returnValues() throws Exception {
        PGVectorStoreConnectionParameters params = new PGVectorStoreConnectionParameters();
        java.lang.reflect.Field hostF = PGVectorStoreConnectionParameters.class.getDeclaredField("host");
        hostF.setAccessible(true);
        hostF.set(params, "localhost");
        java.lang.reflect.Field portF = PGVectorStoreConnectionParameters.class.getDeclaredField("port");
        portF.setAccessible(true);
        portF.set(params, 5432);
        java.lang.reflect.Field dbF = PGVectorStoreConnectionParameters.class.getDeclaredField("database");
        dbF.setAccessible(true);
        dbF.set(params, "default");
        java.lang.reflect.Field userF = PGVectorStoreConnectionParameters.class.getDeclaredField("user");
        userF.setAccessible(true);
        userF.set(params, "postgres");
        java.lang.reflect.Field pwF = PGVectorStoreConnectionParameters.class.getDeclaredField("password");
        pwF.setAccessible(true);
        pwF.set(params, "pw");
        assertEquals("localhost", params.getHost());
        assertEquals(5432, params.getPort());
        assertEquals("default", params.getDatabase());
        assertEquals("postgres", params.getUser());
        assertEquals("pw", params.getPassword());
    }

    @Test
    void getters_nullAndDefaults() {
        PGVectorStoreConnectionParameters params = new PGVectorStoreConnectionParameters();
        assertNull(params.getHost());
        assertEquals(0, params.getPort());
        assertNull(params.getDatabase());
        assertNull(params.getUser());
        assertNull(params.getPassword());
    }
} 