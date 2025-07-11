package org.mule.extension.vectors.internal.store.ephemeralfile;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mule.extension.vectors.internal.connection.store.ephemeralfile.EphemeralFileStoreConnection;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.nio.file.NoSuchFileException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EphemeralFileStoreIteratorTest {

    EphemeralFileStoreConnection connection;
    QueryParameters queryParameters;

    String validJson;
    String invalidJson;

    @BeforeEach
    void setup() {
        connection = Mockito.mock(EphemeralFileStoreConnection.class);
        queryParameters = Mockito.mock(QueryParameters.class);
        validJson = "{" +
                "\"entries\": [" +
                "  {" +
                "    \"id\": \"id1\"," +
                "    \"embedded\": {" +
                "      \"text\": \"hello\"," +
                "      \"metadata\": {\"metadata\": {\"foo\": \"bar\"}}" +
                "    }," +
                "    \"embedding\": {\"vector\": [1.0, 2.0, 3.0]}" +
                "  }" +
                "]}";
        invalidJson = "not a json";
    }

    @Test
    void hasNext_and_next_work_for_valid_json() {
        when(connection.getWorkingDir()).thenReturn("/tmp");
        when(queryParameters.retrieveEmbeddings()).thenReturn(true);
        try (MockedConstruction<EphemeralFileEmbeddingStore> construction =
                Mockito.mockConstruction(EphemeralFileEmbeddingStore.class,
                        (mock, context) -> when(mock.serializeToJson()).thenReturn(validJson))) {
            EphemeralFileStoreIterator<TextSegment> iterator = new EphemeralFileStoreIterator<>(connection, queryParameters, "sotrName");
            assertThat(iterator.hasNext()).isTrue();
            var row = iterator.next();
            assertThat(row.getId()).isEqualTo("id1");
            assertThat(row.getEmbedding().vector()).containsExactly(1.0f, 2.0f, 3.0f);
            assertThat(((TextSegment) row.getEmbedded()).text()).isEqualTo("hello");
            assertThat(((TextSegment) row.getEmbedded()).metadata().getString("foo")).isEqualTo("bar");
            assertThat(iterator.hasNext()).isFalse();
        }
    }

    @Test
    void next_throws_NoSuchElementException_when_out_of_bounds() {
        when(connection.getWorkingDir()).thenReturn("/tmp");
        when(queryParameters.retrieveEmbeddings()).thenReturn(true);
        try (MockedConstruction<EphemeralFileEmbeddingStore> construction =
                Mockito.mockConstruction(EphemeralFileEmbeddingStore.class,
                        (mock, context) -> when(mock.serializeToJson()).thenReturn(validJson))) {
            EphemeralFileStoreIterator<TextSegment> iterator = new EphemeralFileStoreIterator<>(connection, queryParameters,"sotrName");
            iterator.next(); // first
            assertThat(iterator.hasNext()).isFalse();
            assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
        }
    }

    @Test
    void constructor_throws_ModuleException_on_invalid_json() {
        when(connection.getWorkingDir()).thenReturn("/tmp");
        when(queryParameters.retrieveEmbeddings()).thenReturn(true);
        try (MockedConstruction<EphemeralFileEmbeddingStore> construction =
                Mockito.mockConstruction(EphemeralFileEmbeddingStore.class,
                        (mock, context) -> when(mock.serializeToJson()).thenReturn(invalidJson))) {
            assertThatThrownBy(() -> new EphemeralFileStoreIterator<>(connection, queryParameters,"sotrName"))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Invalid file format");
        }
    }

    @Test
    void constructor_throws_ModuleException_on_missing_file() {
        when(connection.getWorkingDir()).thenReturn("/tmp");
        when(queryParameters.retrieveEmbeddings()).thenReturn(true);
        try (MockedConstruction<EphemeralFileEmbeddingStore> construction =
                Mockito.mockConstruction(EphemeralFileEmbeddingStore.class,
                        (mock, context) -> when(mock.serializeToJson()).thenThrow(new RuntimeException(new NoSuchFileException("/tmp/store"))))) {
            assertThatThrownBy(() -> new EphemeralFileStoreIterator<>(connection, queryParameters,"sotrName"))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Store file not found");
        }
    }

    @Test
    void constructor_throws_ModuleException_on_unreadable_file() {
        when(connection.getWorkingDir()).thenReturn("/tmp");
        when(queryParameters.retrieveEmbeddings()).thenReturn(true);
        try (MockedConstruction<EphemeralFileEmbeddingStore> construction =
                Mockito.mockConstruction(EphemeralFileEmbeddingStore.class,
                        (mock, context) -> when(mock.serializeToJson()).thenThrow(new RuntimeException(new java.io.IOException("fail"))))) {
            assertThatThrownBy(() -> new EphemeralFileStoreIterator<>(connection, queryParameters,"sotrName"))
                .isInstanceOf(ModuleException.class)
                .hasMessageContaining("Failed to read store file");
        }
    }
}
