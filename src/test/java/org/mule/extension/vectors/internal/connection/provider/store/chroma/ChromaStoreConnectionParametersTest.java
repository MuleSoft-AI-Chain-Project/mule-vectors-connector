package org.mule.extension.vectors.internal.connection.store.chroma;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.connection.provider.store.chroma.ChromaStoreConnectionParameters;

import static org.junit.jupiter.api.Assertions.*;

class ChromaStoreConnectionParametersTest {

    @Test
    void getters_returnValues() {
        ChromaStoreConnectionParameters params = new ChromaStoreConnectionParameters();
        // Use reflection to set private field
        try {
            java.lang.reflect.Field urlF = ChromaStoreConnectionParameters.class.getDeclaredField("url");
            urlF.setAccessible(true);
            urlF.set(params, "http://localhost:8000");
        } catch (Exception e) {
            fail(e);
        }
        assertEquals("http://localhost:8000", params.getUrl());
    }

    @Test
    void getters_nullByDefault() {
        ChromaStoreConnectionParameters params = new ChromaStoreConnectionParameters();
        assertNull(params.getUrl());
    }
} 