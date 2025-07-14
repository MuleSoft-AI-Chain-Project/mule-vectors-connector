package org.mule.extension.vectors.internal.store.aisearch;

import dev.langchain4j.store.embedding.filter.*;
import dev.langchain4j.store.embedding.filter.comparison.*;
import dev.langchain4j.store.embedding.filter.logical.*;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.service.store.aisearch.VectorsAzureAiSearchFilterMapper;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class VectorsAzureAiSearchFilterMapperTest {
    @Test
    void map_null_returnsEmptyString() {
        VectorsAzureAiSearchFilterMapper mapper = new VectorsAzureAiSearchFilterMapper();
        assertEquals("", mapper.map(null));
    }

    @Test
    void map_logicalAndOrNot() {
        VectorsAzureAiSearchFilterMapper mapper = new VectorsAzureAiSearchFilterMapper();
        Filter left = new IsEqualTo("foo", "bar");
        Filter right = new IsGreaterThan("foo", 1);
        assertTrue(mapper.map(new And(left, right)).contains("and"));
        assertTrue(mapper.map(new Or(left, right)).contains("or"));
        assertTrue(mapper.map(new Not(left)).contains("not"));
    }

    @Test
    void map_comparisonFilters() {
        VectorsAzureAiSearchFilterMapper mapper = new VectorsAzureAiSearchFilterMapper();
        assertTrue(mapper.map(new IsEqualTo("foo", "bar")).contains("eq"));
        assertTrue(mapper.map(new IsGreaterThan("foo", 1)).contains("gt"));
        assertTrue(mapper.map(new IsGreaterThanOrEqualTo("foo", 1)).contains("ge"));
        assertTrue(mapper.map(new IsLessThan("foo", 1)).contains("lt"));
        assertTrue(mapper.map(new IsLessThanOrEqualTo("foo", 1)).contains("le"));
        assertTrue(mapper.map(new IsIn("foo", Arrays.asList("a", "b"))).contains("search.in"));
        assertTrue(mapper.map(new IsNotIn("foo", Arrays.asList("a", "b"))).contains("not"));
        assertTrue(mapper.map(new ContainsString("foo", "bar")).contains("ismatch"));
        assertTrue(mapper.map(new IsNotEqualTo("foo", "bar")).contains("not"));
    }

    @Test
    void map_unsupportedFilter_throws() {
        VectorsAzureAiSearchFilterMapper mapper = new VectorsAzureAiSearchFilterMapper();
        Filter unsupported = new Filter() {
            @Override
            public boolean test(Object o) { return false; }
        };
        assertThrows(UnsupportedOperationException.class, () -> mapper.map(unsupported));
    }
} 
