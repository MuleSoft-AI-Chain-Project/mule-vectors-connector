package org.mule.extension.vectors.internal.store.weaviate;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import java.util.NoSuchElementException;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import org.mule.extension.vectors.internal.service.store.weaviate.WeaviateStoreIterator;

import static org.junit.jupiter.api.Assertions.*;

class WeaviateStoreIteratorTest {
    @Test
    void constructionAndHasNext() {
        QueryParameters params = new QueryParameters();
        WeaviateStoreIterator<?> it = new WeaviateStoreIterator<>(params);
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    void nextThrowsNoSuchElementException() {
        QueryParameters params = new QueryParameters();
        WeaviateStoreIterator<?> it = new WeaviateStoreIterator<>(params);
        assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void nextErrorBranch() {
        WeaviateStoreIterator<Object> it = new WeaviateStoreIterator<>(new QueryParameters()) {
            @Override
            public boolean hasNext() { return true; }
            @Override
            public VectorStoreRow<Object> next() {
                throw new RuntimeException("fail");
            }
        };
        Exception ex = assertThrows(RuntimeException.class, it::next);
        assertEquals("fail", ex.getMessage());
    }
} 
