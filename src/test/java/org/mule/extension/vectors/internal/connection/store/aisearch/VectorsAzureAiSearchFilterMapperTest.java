package org.mule.extension.vectors.internal.connection.store.aisearch;

import dev.langchain4j.store.embedding.filter.*;
import dev.langchain4j.store.embedding.filter.comparison.*;
import dev.langchain4j.store.embedding.filter.logical.And;
import dev.langchain4j.store.embedding.filter.logical.Not;
import dev.langchain4j.store.embedding.filter.logical.Or;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.service.store.aisearch.VectorsAzureAiSearchFilterMapper;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class VectorsAzureAiSearchFilterMapperTest {

    private final VectorsAzureAiSearchFilterMapper mapper = new VectorsAzureAiSearchFilterMapper();

    @Test
    void testMapIsEqualTo() {
        Filter filter = new IsEqualTo("foo", "bar");
        String result = mapper.map(filter);
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("bar"));
        assertTrue(result.contains("eq"));
    }

    @Test
    void testMapIsNotEqualTo() {
        Filter filter = new IsNotEqualTo("foo", "bar");
        String result = mapper.map(filter);
        assertTrue(result.contains("not"));
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("bar"));
    }

    @Test
    void testMapAnd() {
        Filter left = new IsEqualTo("foo", "bar");
        Filter right = new IsGreaterThan("baz", 5);
        Filter and = new And(left, right);
        String result = mapper.map(and);
        assertTrue(result.contains("and"));
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("baz"));
    }

    @Test
    void testMapOr() {
        Filter left = new IsEqualTo("foo", "bar");
        Filter right = new IsGreaterThan("baz", 5);
        Filter or = new Or(left, right);
        String result = mapper.map(or);
        assertTrue(result.contains("or"));
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("baz"));
    }

    @Test
    void testMapNot() {
        Filter inner = new IsEqualTo("foo", "bar");
        Filter not = new Not(inner);
        String result = mapper.map(not);
        assertTrue(result.contains("not"));
        assertTrue(result.contains("foo"));
    }

    @Test
    void testMapContainsString() {
        Filter filter = new ContainsString("foo", "bar");
        String result = mapper.map(filter);
        assertTrue(result.contains("ismatch"));
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("bar"));
    }

    @Test
    void testMapIsIn() {
        Filter filter = new IsIn("foo", Arrays.asList("bar", "baz"));
        String result = mapper.map(filter);
        assertTrue(result.contains("search.in"));
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("bar"));
        assertTrue(result.contains("baz"));
    }

    @Test
    void testMapIsNotIn() {
        Filter filter = new IsNotIn("foo", Arrays.asList("bar", "baz"));
        String result = mapper.map(filter);
        assertTrue(result.contains("not"));
        assertTrue(result.contains("foo"));
        assertTrue(result.contains("bar"));
        assertTrue(result.contains("baz"));
    }

    @Test
    void testMapNullReturnsEmptyString() {
        String result = mapper.map(null);
        assertEquals("", result);
    }
} 
