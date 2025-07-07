package org.mule.extension.vectors.internal.helper.provider;

import org.junit.jupiter.api.Test;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueResolvingException;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class FileParserTypeProviderTest {

    @Test
    void resolve_shouldReturnAllParserTypes() throws ValueResolvingException {
        FileParserTypeProvider provider = new FileParserTypeProvider();
        Set<Value> values = provider.resolve();
        Set<String> actual = values.stream().map(Value::getId).collect(Collectors.toSet());
        assertThat(actual).isNotEmpty(); // Replace with expected set if known
    }

    @Test
    void getId_shouldReturnClassName() {
        assertThat(new FileParserTypeProvider().getId()).isEqualTo(FileParserTypeProvider.class.getName());
    }
} 