package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class SegmentationParametersTest {

  @Test
  void getMaxSegmentSizeInChars_shouldDefaultToZero() {
    SegmentationParameters params = new SegmentationParameters();
    assertThat(params.getMaxSegmentSizeInChars()).isZero();
  }

  @Test
  void getMaxSegmentSizeInChars_shouldReflectSetValue() throws Exception {
    SegmentationParameters params = new SegmentationParameters();
    Field field = params.getClass().getDeclaredField("maxSegmentSizeInChars");
    field.setAccessible(true);
    field.set(params, 123);
    assertThat(params.getMaxSegmentSizeInChars()).isEqualTo(123);
  }

  @Test
  void getMaxOverlapSizeInChars_shouldDefaultToZero() {
    SegmentationParameters params = new SegmentationParameters();
    assertThat(params.getMaxOverlapSizeInChars()).isZero();
  }

  @Test
  void getMaxOverlapSizeInChars_shouldReflectSetValue() throws Exception {
    SegmentationParameters params = new SegmentationParameters();
    Field field = params.getClass().getDeclaredField("maxOverlapSizeInChars");
    field.setAccessible(true);
    field.set(params, 42);
    assertThat(params.getMaxOverlapSizeInChars()).isEqualTo(42);
  }
}
