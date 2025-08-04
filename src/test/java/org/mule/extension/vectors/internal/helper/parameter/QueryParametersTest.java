package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class QueryParametersTest {

  @Test
  void retrieveEmbeddings_shouldDefaultToFalse() {
    QueryParameters params = new QueryParameters();
    assertThat(params.retrieveEmbeddings()).isFalse();
  }

  @Test
  void retrieveEmbeddings_shouldReflectSetValue() throws Exception {
    QueryParameters params = new QueryParameters();
    Field field = params.getClass().getDeclaredField("retrieveEmbeddings");
    field.setAccessible(true);
    field.set(params, true);
    assertThat(params.retrieveEmbeddings()).isTrue();
  }

  @Test
  void pageSize_shouldDefaultTo5000() {
    QueryParameters params = new QueryParameters();
    assertThat(params.pageSize()).isEqualTo(5000);
  }

  @Test
  void pageSize_shouldReflectSetValue() throws Exception {
    QueryParameters params = new QueryParameters();
    Field field = params.getClass().getDeclaredField("pageSize");
    field.setAccessible(true);
    field.set(params, 123);
    assertThat(params.pageSize()).isEqualTo(123);
  }
}
