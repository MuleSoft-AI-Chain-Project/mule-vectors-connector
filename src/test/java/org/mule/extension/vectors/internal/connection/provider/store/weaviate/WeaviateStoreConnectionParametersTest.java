package org.mule.extension.vectors.internal.connection.store.weaviate;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.weaviate.WeaviateStoreConnectionParameters;

import static org.junit.jupiter.api.Assertions.*;

class WeaviateStoreConnectionParametersTest {
    @Test
    void getters_returnValues() throws Exception {
        WeaviateStoreConnectionParameters params = new WeaviateStoreConnectionParameters();
        java.lang.reflect.Field schemeF = WeaviateStoreConnectionParameters.class.getDeclaredField("scheme");
        schemeF.setAccessible(true);
        schemeF.set(params, "https");
        java.lang.reflect.Field hostF = WeaviateStoreConnectionParameters.class.getDeclaredField("host");
        hostF.setAccessible(true);
        hostF.set(params, "localhost");
        java.lang.reflect.Field portF = WeaviateStoreConnectionParameters.class.getDeclaredField("port");
        portF.setAccessible(true);
        portF.set(params, 8181);
        java.lang.reflect.Field grpcPortF = WeaviateStoreConnectionParameters.class.getDeclaredField("grpcPort");
        grpcPortF.setAccessible(true);
        grpcPortF.set(params, 50051);
        java.lang.reflect.Field apiKeyF = WeaviateStoreConnectionParameters.class.getDeclaredField("apiKey");
        apiKeyF.setAccessible(true);
        apiKeyF.set(params, "secret");
        java.lang.reflect.Field avoidDupsF = WeaviateStoreConnectionParameters.class.getDeclaredField("avoidDups");
        avoidDupsF.setAccessible(true);
        avoidDupsF.set(params, true);
        java.lang.reflect.Field consistencyF = WeaviateStoreConnectionParameters.class.getDeclaredField("consistencyLevel");
        consistencyF.setAccessible(true);
        consistencyF.set(params, "ALL");
        java.lang.reflect.Field securedGrpcF = WeaviateStoreConnectionParameters.class.getDeclaredField("securedGrpc");
        securedGrpcF.setAccessible(true);
        securedGrpcF.set(params, true);
        java.lang.reflect.Field useGrpcF = WeaviateStoreConnectionParameters.class.getDeclaredField("useGrpcForInserts");
        useGrpcF.setAccessible(true);
        useGrpcF.set(params, true);

        assertEquals("https", params.getScheme());
        assertEquals("localhost", params.getHost());
        assertEquals(8181, params.getPort());
        assertEquals(50051, params.getGrpcPort());
        assertEquals("secret", params.getApiKey());
        assertTrue(params.isAvoidDups());
        assertEquals("ALL", params.getConsistencyLevel());
        assertTrue(params.isSecuredGrpc());
        assertTrue(params.isUseGrpcForInserts());
    }

    @Test
    void getters_nullOrDefaultByDefault() {
        WeaviateStoreConnectionParameters params = new WeaviateStoreConnectionParameters();
        assertNull(params.getScheme());
        assertNull(params.getHost());
        assertNull(params.getPort());
        assertNull(params.getGrpcPort());
        assertNull(params.getApiKey());
        assertFalse(params.isAvoidDups()); // default is false unless set
        assertNull(params.getConsistencyLevel());
        assertFalse(params.isSecuredGrpc());
        assertFalse(params.isUseGrpcForInserts());
    }
} 