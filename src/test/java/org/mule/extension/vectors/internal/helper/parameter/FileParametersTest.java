package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

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
