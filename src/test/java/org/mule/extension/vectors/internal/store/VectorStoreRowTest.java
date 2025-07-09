package org.mule.extension.vectors.internal.store;

import dev.langchain4j.data.embedding.Embedding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class VectorStoreRowTest {

    static Embedding embedding(float... values) {
        // Simple Embedding stub (replace with real if available)
        return Embedding.from(List.of(values[0], values.length > 1 ? values[1] : 0f));
    }

    @Test
    void constructorAndGetters_withEmbedded() {
        Embedding emb = embedding(1.0f, 2.0f);
        String embedded = "data";
        VectorStoreRow<String> row = new VectorStoreRow<>("id1", emb, embedded);
        assertThat(row.getId()).isEqualTo("id1");
        assertThat(row.getEmbedding()).isEqualTo(emb);
        assertThat(row.getEmbedded()).isEqualTo(embedded);
    }

    @Test
    void constructorAndGetters_withoutEmbedded() {
        Embedding emb = embedding(3.0f, 4.0f);
        VectorStoreRow<String> row = new VectorStoreRow<>("id2", emb);
        assertThat(row.getId()).isEqualTo("id2");
        assertThat(row.getEmbedding()).isEqualTo(emb);
        assertThat(row.getEmbedded()).isNull();
    }

    @Test
    void equalsAndHashCode() {
        Embedding emb1 = embedding(1.0f, 2.0f);
        Embedding emb2 = embedding(1.0f, 2.0f);
        VectorStoreRow<String> row1 = new VectorStoreRow<>("id", emb1, "foo");
        VectorStoreRow<String> row2 = new VectorStoreRow<>("id", emb2, "foo");
        VectorStoreRow<String> row3 = new VectorStoreRow<>("id", emb1, "bar");
        VectorStoreRow<String> row4 = new VectorStoreRow<>("id2", emb1, "foo");
        assertThat(row1).isEqualTo(row2);
        assertThat(row1.hashCode()).isEqualTo(row2.hashCode());
        assertThat(row1).isNotEqualTo(row3);
        assertThat(row1).isNotEqualTo(row4);
        assertThat(row1).isNotEqualTo(null);
        assertThat(row1).isNotEqualTo("not a row");
        assertThat(row1).isEqualTo(row1); // self
    }

    @Test
    void validationFailsOnBlankId() {
        Embedding emb = embedding(1.0f, 2.0f);
        assertThatThrownBy(() -> new VectorStoreRow<>("", emb))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> new VectorStoreRow<>(null, emb))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
    }
} 