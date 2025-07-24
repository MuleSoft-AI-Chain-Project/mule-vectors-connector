package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class EmbeddingModelParametersTest {

  @Test
  void getEmbeddingModelName_shouldDefaultToNull() {
    EmbeddingModelParameters params = new EmbeddingModelParameters();
    assertThat(params.getEmbeddingModelName()).isNull();
  }

  @Test
  void getEmbeddingModelName_shouldReflectSetValue() throws Exception {
    EmbeddingModelParameters params = new EmbeddingModelParameters();
    Field field = params.getClass().getDeclaredField("embeddingModelName");
    field.setAccessible(true);
    field.set(params, "text-embedding-ada-002");
    assertThat(params.getEmbeddingModelName()).isEqualTo("text-embedding-ada-002");
  }

  @Test
  void getEmbeddingModelType_shouldReturnExpectedType() throws Exception {
    EmbeddingModelParameters params = new EmbeddingModelParameters();
    Field field = params.getClass().getDeclaredField("embeddingModelName");
    field.setAccessible(true);
    field.set(params, "text-embedding-ada-002");
    assertThat(params.getEmbeddingModelType()).isEqualTo(EmbeddingModelHelper.EmbeddingModelType.TEXT);
  }
}
