package org.mule.extension.vectors.internal.store.ephemeralfile;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.extension.vectors.internal.connection.provider.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.ephemeralfile.EphemeralFileEmbeddingStore;
import org.mule.extension.vectors.internal.service.store.ephemeralfile.EphemeralFileStore;
import org.mule.runtime.extension.api.exception.ModuleException;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EphemeralFileStoreTest {

  @Mock
  StoreConfiguration storeConfiguration;
  @Mock
  EphemeralFileStoreConnection connection;
  @Mock
  QueryParameters queryParameters;

  @Test
  void constructor_assignsFields() {
    when(connection.getWorkingDir()).thenReturn("/tmp/store");
    EphemeralFileStore store = new EphemeralFileStore(storeConfiguration, connection, "testStore", queryParameters, 128, false);
    assertThat(store).isNotNull();
  }

  @Test
  void getEphemeralFileStorePath_withWorkingDir() {
    when(connection.getWorkingDir()).thenReturn("/tmp/store");
    EphemeralFileStore store = new EphemeralFileStore(storeConfiguration, connection, "testStore", queryParameters, 128, false);
    assertThat(store.getEphemeralFileStorePath()).isEqualTo("/tmp/store/testStore.store");
  }

  @Test
  void getEphemeralFileStorePath_withNullWorkingDir() {
    when(connection.getWorkingDir()).thenReturn(null);
    EphemeralFileStore store = new EphemeralFileStore(storeConfiguration, connection, "testStore", queryParameters, 128, false);
    assertThat(store.getEphemeralFileStorePath()).isEqualTo("testStore.store");
  }

  @Test
  void getEphemeralFileStorePath_withBlankWorkingDir() {
    when(connection.getWorkingDir()).thenReturn("  ");
    EphemeralFileStore store = new EphemeralFileStore(storeConfiguration, connection, "testStore", queryParameters, 128, false);
    assertThat(store.getEphemeralFileStorePath()).isEqualTo("testStore.store");
  }

  @Test
  void buildEmbeddingStore_returnsEphemeralFileEmbeddingStore() {
    when(connection.getWorkingDir()).thenReturn("/tmp");
    EphemeralFileStore store = new EphemeralFileStore(storeConfiguration, connection, "testStore", queryParameters, 128, false);
    EmbeddingStore<TextSegment> result = store.buildEmbeddingStore();
    assertThat(result).isNotNull().isInstanceOf(EphemeralFileEmbeddingStore.class);
  }
}
