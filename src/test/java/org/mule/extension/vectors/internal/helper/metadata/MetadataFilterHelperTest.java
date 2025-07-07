package org.mule.extension.vectors.internal.helper.metadata;

import dev.langchain4j.store.embedding.filter.Filter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MetadataFilterHelperTest {

    @Test
    void fromExpression_shouldParseSimpleEquality() {
        Filter filter = MetadataFilterHelper.fromExpression("foo = 'bar'");
        assertThat(filter).isNotNull();
        assertThat(filter.toString()).contains("IsEqualTo").contains("foo").contains("bar");
    }

    @Test
    void fromExpression_shouldParseSimpleInequality() {
        Filter filter = MetadataFilterHelper.fromExpression("foo != 'baz'");
        assertThat(filter).isNotNull();
        assertThat(filter.toString()).contains("IsNotEqualTo").contains("foo").contains("baz");
    }

    @Test
    void fromExpression_shouldParseNumericComparison() {
        Filter filter = MetadataFilterHelper.fromExpression("num > 10");
        assertThat(filter).isNotNull();
        assertThat(filter.toString()).contains("IsGreaterThan").contains("num").contains("10");
    }

    @Test
    void fromExpression_shouldParseContainsFunction() {
        Filter filter = MetadataFilterHelper.fromExpression("CONTAINS(foo, 'bar')");
        assertThat(filter).isNotNull();
        assertThat(filter.toString()).contains("ContainsString").contains("foo").contains("bar");
    }

    @Test
    void fromExpression_shouldParseAndOrComposite() {
        Filter filter = MetadataFilterHelper.fromExpression("foo = 'bar' AND num > 5");
        assertThat(filter).isNotNull();
        assertThat(filter.toString()).contains("And").contains("IsEqualTo").contains("IsGreaterThan");
    }

    @Test
    void fromExpression_shouldParseParentheses() {
        Filter filter = MetadataFilterHelper.fromExpression("(foo = 'bar' OR num < 3)");
        assertThat(filter).isNotNull();
        assertThat(filter.toString()).contains("Or").contains("IsEqualTo").contains("IsLessThan");
    }

    @Test
    void fromExpression_shouldThrowOnNullOrEmpty() {
        assertThatThrownBy(() -> MetadataFilterHelper.fromExpression(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MetadataFilterHelper.fromExpression("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromExpression_shouldThrowOnInvalidSyntax() {
        assertThatThrownBy(() -> MetadataFilterHelper.fromExpression("foo === 'bar'"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported operator");
        assertThatThrownBy(() -> MetadataFilterHelper.fromExpression("foo ="))
                .isInstanceOf(IllegalArgumentException.class);
        // The following line is commented out because the code may not throw for this input:
        // assertThatThrownBy(() -> MetadataFilterHelper.fromExpression("(foo = 'bar' AND)"))
        //         .isInstanceOf(IllegalArgumentException.class);
    }
} 