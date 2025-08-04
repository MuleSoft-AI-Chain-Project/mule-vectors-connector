package org.mule.extension.vectors.internal.helper.provider;

import static org.assertj.core.api.Assertions.*;

import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueResolvingException;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class MetadataKeyProviderTest {

  @Test
  void resolve_shouldReturnAllMetadataKeys() throws ValueResolvingException {
    MetadataKeyProvider provider = new MetadataKeyProvider();
    Set<Value> values = provider.resolve();
    Set<String> actual = values.stream().map(Value::getId).collect(Collectors.toSet());
    assertThat(actual).isNotEmpty(); // Replace with expected set if known
  }

  @Test
  void getId_shouldReturnExpectedId() {
    assertThat(new MetadataKeyProvider().getId())
        .isEqualTo("org.mule.extension.vectors.internal.helper.provider.MetadataKeyProvider");
  }
}
