package org.mule.extension.vectors.internal.helper.provider;

import io.milvus.param.MetricType;
import org.junit.jupiter.api.Test;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueResolvingException;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class MilvusMetricTypeProviderTest {

    @Test
    void resolve_shouldReturnAllMetricTypes() throws ValueResolvingException {
        MilvusMetricTypeProvider provider = new MilvusMetricTypeProvider();
        Set<Value> values = provider.resolve();
        Set<String> actual = values.stream().map(Value::getId).collect(Collectors.toSet());
        Set<String> expected = java.util.Arrays.stream(MetricType.values()).map(Enum::name).collect(Collectors.toSet());
        assertThat(actual).isEqualTo(expected);
        assertThat(values).hasSize(MetricType.values().length);
    }

    @Test
    void getId_shouldReturnExpectedId() {
        assertThat(new MilvusMetricTypeProvider().getId()).isEqualTo("Milvus MetricType Value Provider");
    }
} 