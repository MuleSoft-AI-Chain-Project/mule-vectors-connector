package org.mule.extension.vectors.internal.helper.provider;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import io.milvus.param.MetricType;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;
import org.mule.runtime.extension.api.values.ValueResolvingException;

public class MilvusMetricTypeProvider implements ValueProvider {

  @Override
  public Set<Value> resolve() throws ValueResolvingException {

    // Get all enum values and convert to Value objects
    return Arrays.stream(MetricType.values())
        .map(metricType -> ValueBuilder.newValue(metricType.name())
            .withDisplayName(metricType.name())
            .build())
        .collect(Collectors.toSet());
  }

  @Override
  public String getId() {
    return "Milvus MetricType Value Provider";
  }
}
