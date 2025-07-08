package org.mule.extension.vectors.internal.connection.store.pgvector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.store.BaseStoreConnection;
import org.mule.runtime.api.connection.ConnectionException;

import static org.junit.jupiter.api.Assertions.*;

class PGVectorStoreConnectionProviderTest {

    PGVectorStoreConnectionProvider provider;
    PGVectorStoreConnectionParameters params;

    @BeforeEach
    void setUp() {
        provider = new PGVectorStoreConnectionProvider();
        params = new PGVectorStoreConnectionParameters();
        try {
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
        } catch (Exception e) {
            fail(e);
        }
        provider.setPGVectorStoreConnectionParameters(params);
    }

    @Test
    void connect_returnsConnection() throws Exception {
        provider.initialise();
        BaseStoreConnection conn = provider.connect();
        assertNotNull(conn);
        assertTrue(conn instanceof PGVectorStoreConnection);
    }

    @Test
    void dispose_doesNotThrow() throws Exception {
        provider.initialise();
        // Replace the real connection with a test double that does not throw
        PGVectorStoreConnection testConn = new PGVectorStoreConnection(params) {
            @Override public void dispose() {}
        };
        java.lang.reflect.Field f = PGVectorStoreConnectionProvider.class.getDeclaredField("pgVectorStoreConnection");
        f.setAccessible(true);
        f.set(provider, testConn);
        assertDoesNotThrow(provider::dispose);
    }
} 