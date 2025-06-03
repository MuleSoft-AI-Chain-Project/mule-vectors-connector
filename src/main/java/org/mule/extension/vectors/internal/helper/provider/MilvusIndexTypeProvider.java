package org.mule.extension.vectors.internal.helper.provider;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import io.milvus.param.IndexType;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;
import org.mule.runtime.extension.api.values.ValueResolvingException;

public class MilvusIndexTypeProvider implements ValueProvider {

  @Override
  public Set<Value> resolve() throws ValueResolvingException {

    // Get all enum values and convert to Value objects
    return Arrays.stream(IndexType.values())
        .map(indexType -> ValueBuilder.newValue(indexType.name())
            .withDisplayName(indexType.name())
            .build())
        .collect(Collectors.toSet());
  }

  @Override
  public String getId() {
    return "Milvus IndexType Value Provider";
  }
}
