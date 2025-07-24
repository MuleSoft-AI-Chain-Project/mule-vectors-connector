package org.mule.extension.vectors.internal.store.ephemeralfile;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.service.store.ephemeralfile.EphemeralFileEmbeddingStore;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class EphemeralFileEmbeddingStoreMockedConstructionTest {

  @Test
  void testMockedConstructionForEphemeralFileEmbeddingStore() {
    try (MockedConstruction<EphemeralFileEmbeddingStore> construction = mockConstruction(EphemeralFileEmbeddingStore.class,
                                                                                         (mock,
                                                                                          context) -> when(mock.serializeToJson())
                                                                                              .thenReturn("mocked-json"))) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore("/tmp");
      assertThat(store.serializeToJson()).isEqualTo("mocked-json");
    }
  }
}
