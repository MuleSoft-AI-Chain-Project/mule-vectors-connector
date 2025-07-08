package org.mule.extension.vectors.internal.helper.parameter;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.HashMap;
import static org.assertj.core.api.Assertions.*;

class CustomMetadataTest {
    @Test
    void getMetadataEntries_shouldDefaultToNull() {
        CustomMetadata params = new CustomMetadata();
        assertThat(params.getMetadataEntries()).isNull();
    }

    @Test
    void getMetadataEntries_shouldReflectSetValue() throws Exception {
        CustomMetadata params = new CustomMetadata();
        Field field = params.getClass().getDeclaredField("metadataEntries");
        field.setAccessible(true);
        HashMap<String, String> map = new HashMap<>();
        map.put("foo", "bar");
        field.set(params, map);
        assertThat(params.getMetadataEntries()).containsEntry("foo", "bar");
    }
} 