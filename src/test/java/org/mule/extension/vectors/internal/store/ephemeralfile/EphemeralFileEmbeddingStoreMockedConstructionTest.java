package org.mule.extension.vectors.internal.store.ephemeralfile;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mule.extension.vectors.internal.service.store.ephemeralfile.EphemeralFileEmbeddingStore;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class EphemeralFileEmbeddingStoreMockedConstructionTest {
    @Test
    void testMockedConstructionForEphemeralFileEmbeddingStore() {
        try (MockedConstruction<EphemeralFileEmbeddingStore> construction = mockConstruction(EphemeralFileEmbeddingStore.class,
                                                                                             (mock, context) -> when(mock.serializeToJson()).thenReturn("mocked-json"))) {
            EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore("/tmp");
            assertThat(store.serializeToJson()).isEqualTo("mocked-json");
        }
    }
} 
