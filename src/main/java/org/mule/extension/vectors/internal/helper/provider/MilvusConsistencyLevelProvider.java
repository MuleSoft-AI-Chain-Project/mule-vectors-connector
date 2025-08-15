package org.mule.extension.vectors.internal.helper.provider;

import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;
import org.mule.runtime.extension.api.values.ValueResolvingException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import io.milvus.common.clientenum.ConsistencyLevelEnum;

public class MilvusConsistencyLevelProvider implements ValueProvider {

  @Override
  public Set<Value> resolve() throws ValueResolvingException {

    // Get all enum values and convert to Value objects
    return Arrays.stream(ConsistencyLevelEnum.values())
        .map(consistencyLevel -> ValueBuilder.newValue(consistencyLevel.name())
            .withDisplayName(consistencyLevel.name())
            .build())
        .collect(Collectors.toSet());
  }

  @Override
  public String getId() {
    return "Milvus ConsistencyLevel Value Provider";
  }
}

