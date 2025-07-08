package org.mule.extension.vectors.internal.helper.parameter;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.assertj.core.api.Assertions.*;

class FileParametersTest {
    @Test
    void getContextPath_shouldDefaultToNull() {
        FileParameters params = new FileParameters();
        assertThat(params.getContextPath()).isNull();
    }

    @Test
    void getContextPath_shouldReflectSetValue() throws Exception {
        FileParameters params = new FileParameters();
        Field field = params.getClass().getDeclaredField("contextPath");
        field.setAccessible(true);
        field.set(params, "/test/path");
        assertThat(params.getContextPath()).isEqualTo("/test/path");
    }
} 