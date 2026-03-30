package org.mule.extension.vectors.internal.store.ephemeralfile;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.service.store.ephemeralfile.EphemeralFileEmbeddingStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class EphemeralFileEmbeddingStoreTest {

  String path;

  @BeforeEach
  void setup() {
    path = "/tmp/test.store";
  }

  @Test
  void constructor_assigns_path() {
    EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
    assertThat(store).isNotNull();
  }

  @Test
  void add_and_serializeToFile_are_called() {
    Embedding embedding = mock(Embedding.class);
    try (MockedConstruction<InMemoryEmbeddingStore> construction =
        Mockito.mockConstruction(InMemoryEmbeddingStore.class,
                                 (mock, context) -> when(mock.add(embedding)).thenReturn("id1"))) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      String id = store.add(embedding);
      assertThat(id).isEqualTo("id1");
      verify(construction.constructed().get(0)).serializeToFile(path);
    }
  }

  @Test
  void addAll_and_serializeToFile_are_called() {
    List<Embedding> embeddings = List.of(mock(Embedding.class));
    try (MockedConstruction<InMemoryEmbeddingStore> construction =
        Mockito.mockConstruction(InMemoryEmbeddingStore.class,
                                 (mock, context) -> when(mock.addAll(embeddings)).thenReturn(List.of("id1")))) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      List<String> ids = store.addAll(embeddings);
      assertThat(ids).containsExactly("id1");
      verify(construction.constructed().get(0)).serializeToFile(path);
    }
  }

  @Test
  void removeAll_by_ids_and_serializeToFile_are_called() {
    List<String> ids = List.of("id1");
    try (MockedConstruction<InMemoryEmbeddingStore> construction =
        Mockito.mockConstruction(InMemoryEmbeddingStore.class)) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      store.removeAll(ids);
      verify(construction.constructed().get(0)).removeAll(ids);
      verify(construction.constructed().get(0)).serializeToFile(path);
    }
  }

  @Test
  void removeAll_by_filter_and_serializeToFile_are_called() {
    Filter filter = mock(Filter.class);
    try (MockedConstruction<InMemoryEmbeddingStore> construction =
        Mockito.mockConstruction(InMemoryEmbeddingStore.class)) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      store.removeAll(filter);
      verify(construction.constructed().get(0)).removeAll(filter);
      verify(construction.constructed().get(0)).serializeToFile(path);
    }
  }

  @Test
  void removeAll_deletes_file() throws IOException {
    try (var files = mockStatic(Files.class)) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      store.removeAll();
      files.verify(() -> Files.deleteIfExists(Paths.get(path)));
    }
  }

  @Test
  void search_delegates_to_memStore() {
    EmbeddingSearchRequest request = mock(EmbeddingSearchRequest.class);
    EmbeddingSearchResult<TextSegment> result = mock(EmbeddingSearchResult.class);
    try (MockedConstruction<InMemoryEmbeddingStore> construction =
        Mockito.mockConstruction(InMemoryEmbeddingStore.class,
                                 (mock, context) -> when(mock.search(request)).thenReturn(result))) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      EmbeddingSearchResult<TextSegment> actual = store.search(request);
      assertThat(actual).isSameAs(result);
    }
  }

  @Test
  void serializeToJson_delegates_to_memStore() {
    try (MockedConstruction<InMemoryEmbeddingStore> construction =
        Mockito.mockConstruction(InMemoryEmbeddingStore.class,
                                 (mock, context) -> when(mock.serializeToJson()).thenReturn("{}\n"))) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      String json = store.serializeToJson();
      assertThat(json).isEqualTo("{}\n");
    }
  }

  @Test
  void add_withIdAndEmbedding_delegates() {
    Embedding embedding = mock(Embedding.class);
    try (MockedConstruction<InMemoryEmbeddingStore> construction =
        Mockito.mockConstruction(InMemoryEmbeddingStore.class)) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      store.add("myId", embedding);
      verify(construction.constructed().get(0)).add("myId", embedding);
      verify(construction.constructed().get(0)).serializeToFile(path);
    }
  }

  @Test
  void add_withEmbeddingAndTextSegment_delegates() {
    Embedding embedding = mock(Embedding.class);
    TextSegment segment = mock(TextSegment.class);
    try (MockedConstruction<InMemoryEmbeddingStore> construction =
        Mockito.mockConstruction(InMemoryEmbeddingStore.class,
                                 (mock, context) -> when(mock.add(embedding, segment)).thenReturn("id2"))) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      String id = store.add(embedding, segment);
      assertThat(id).isEqualTo("id2");
      verify(construction.constructed().get(0)).serializeToFile(path);
    }
  }

  @Test
  void add_withIdEmbeddingAndTextSegment_delegates() {
    Embedding embedding = mock(Embedding.class);
    TextSegment segment = mock(TextSegment.class);
    try (MockedConstruction<InMemoryEmbeddingStore> construction =
        Mockito.mockConstruction(InMemoryEmbeddingStore.class)) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      store.add("myId", embedding, segment);
      verify(construction.constructed().get(0)).add("myId", embedding, segment);
      verify(construction.constructed().get(0)).serializeToFile(path);
    }
  }

  @Test
  void addAll_withIdsEmbeddingsAndSegments_delegates() {
    List<String> ids = List.of("id1");
    List<Embedding> embeddings = List.of(mock(Embedding.class));
    List<TextSegment> segments = List.of(mock(TextSegment.class));
    try (MockedConstruction<InMemoryEmbeddingStore> construction =
        Mockito.mockConstruction(InMemoryEmbeddingStore.class)) {
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      store.addAll(ids, embeddings, segments);
      verify(construction.constructed().get(0)).addAll(ids, embeddings, segments);
      verify(construction.constructed().get(0)).serializeToFile(path);
    }
  }

  @Test
  void removeAll_throwsModuleExceptionOnIOException() {
    try (var files = mockStatic(Files.class)) {
      files.when(() -> Files.deleteIfExists(Paths.get(path))).thenThrow(new IOException("delete failed"));
      EphemeralFileEmbeddingStore store = new EphemeralFileEmbeddingStore(path);
      assertThatThrownBy(store::removeAll)
          .isInstanceOf(org.mule.runtime.extension.api.exception.ModuleException.class)
          .hasMessageContaining("delete failed");
    }
  }
}
