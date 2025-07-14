package org.mule.extension.vectors.internal.store.aisearch;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.aisearch.AISearchStoreIterator;

import java.util.NoSuchElementException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AISearchStoreIteratorTest {
    // Utility to allocate instance without running constructor
    @SuppressWarnings("unchecked")
    private static <T> T allocateInstance(Class<T> clazz) {
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) f.get(null);
            return (T) unsafe.allocateInstance(clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void constructionAndHasNext_emptyBatch() {
        var params = new org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnectionParameters();
        org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection conn = new org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection(params, null) {
            @Override public String getApiKey() { return "key"; }
            @Override public String getUrl() { return "endpoint"; }
        };
        QueryParameters queryParams = new QueryParameters();
        AISearchStoreIterator<Object> it = allocateInstance(AISearchStoreIterator.class);
        try {
            Field storeNameF = AISearchStoreIterator.class.getDeclaredField("storeName");
            storeNameF.setAccessible(true);
            storeNameF.set(it, "testStore");
            Field queryParamsF = AISearchStoreIterator.class.getDeclaredField("queryParams");
            queryParamsF.setAccessible(true);
            queryParamsF.set(it, queryParams);
            Field connF = AISearchStoreIterator.class.getDeclaredField("aiSearchStoreConnection");
            connF.setAccessible(true);
            connF.set(it, conn);
            Field dimF = AISearchStoreIterator.class.getDeclaredField("dimension");
            dimF.setAccessible(true);
            dimF.set(it, 128);
            Field hasMoreF = AISearchStoreIterator.class.getDeclaredField("hasMore");
            hasMoreF.setAccessible(true);
            hasMoreF.set(it, false);
            Field batchF = AISearchStoreIterator.class.getDeclaredField("currentBatch");
            batchF.setAccessible(true);
            batchF.set(it, java.util.Collections.emptyIterator());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test
    void next_throwsNoSuchElementException_whenNoNext() {
        var params = new org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnectionParameters();
        org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection conn = new org.mule.extension.vectors.internal.connection.store.aisearch.AISearchStoreConnection(params, null) {
            @Override public String getApiKey() { return "key"; }
            @Override public String getUrl() { return "endpoint"; }
        };
        QueryParameters queryParams = new QueryParameters();
        AISearchStoreIterator<Object> it = allocateInstance(AISearchStoreIterator.class);
        try {
            Field storeNameF = AISearchStoreIterator.class.getDeclaredField("storeName");
            storeNameF.setAccessible(true);
            storeNameF.set(it, "testStore");
            Field queryParamsF = AISearchStoreIterator.class.getDeclaredField("queryParams");
            queryParamsF.setAccessible(true);
            queryParamsF.set(it, queryParams);
            Field connF = AISearchStoreIterator.class.getDeclaredField("aiSearchStoreConnection");
            connF.setAccessible(true);
            connF.set(it, conn);
            Field dimF = AISearchStoreIterator.class.getDeclaredField("dimension");
            dimF.setAccessible(true);
            dimF.set(it, 128);
            Field hasMoreF = AISearchStoreIterator.class.getDeclaredField("hasMore");
            hasMoreF.setAccessible(true);
            hasMoreF.set(it, false);
            Field batchF = AISearchStoreIterator.class.getDeclaredField("currentBatch");
            batchF.setAccessible(true);
            batchF.set(it, java.util.Collections.emptyIterator());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThrows(NoSuchElementException.class, it::next);
    }
} 
