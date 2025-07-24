package org.mule.extension.vectors.internal.helper.provider;

import static org.assertj.core.api.Assertions.*;

import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueResolvingException;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class EmbeddingModelNameProviderTest {

  @Test
  @Disabled("Requires modelConnection to be set; not testable in isolation.")
  void resolve_shouldReturnAllModelNames() throws ValueResolvingException {
    EmbeddingModelNameProvider provider = new EmbeddingModelNameProvider();
    Set<Value> values = provider.resolve();
    Set<String> actual = values.stream().map(Value::getId).collect(Collectors.toSet());
    assertThat(actual).isNotEmpty();
  }

  @Test
  void getId_shouldReturnClassName() {
    assertThat(new EmbeddingModelNameProvider().getId()).isEqualTo(EmbeddingModelNameProvider.class.getName());
  }
}
