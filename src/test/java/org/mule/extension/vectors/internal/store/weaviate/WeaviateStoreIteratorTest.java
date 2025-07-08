package org.mule.extension.vectors.internal.store.weaviate;

import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WeaviateStoreIteratorTest {
    @Mock QueryParameters queryParameters;

    @Test
    void hasNext_returnsFalse() {
        WeaviateStoreIterator<TextSegment> iterator = new WeaviateStoreIterator<>(queryParameters);
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    void next_throwsNoSuchElementException() {
        WeaviateStoreIterator<TextSegment> iterator = new WeaviateStoreIterator<>(queryParameters);
        assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
    }
} 