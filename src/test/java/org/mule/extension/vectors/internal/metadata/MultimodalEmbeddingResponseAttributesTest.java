package org.mule.extension.vectors.internal.metadata;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.api.metadata.MultimodalEmbeddingResponseAttributes;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

class MultimodalEmbeddingResponseAttributesTest {

  @Test
  void constructor_extractsAllFields() {
    HashMap<String, Object> attrs = new HashMap<>();
    attrs.put("embeddingModelName", "test-model");
    attrs.put("embeddingModelDimension", 128);
    attrs.put("mimeType", "image/png");
    attrs.put("mediaType", "image");
    MultimodalEmbeddingResponseAttributes result = new MultimodalEmbeddingResponseAttributes(attrs);
    assertThat(result.getEmbeddingModelName()).isEqualTo("test-model");
    assertThat(result.getEmbeddingModelDimension()).isEqualTo(128);
    assertThat(result.getMimeType()).isEqualTo("image/png");
    assertThat(result.getMediaType()).isEqualTo("image");
  }

  @Test
  void constructor_withNullMimeTypeAndMediaType() {
    HashMap<String, Object> attrs = new HashMap<>();
    attrs.put("embeddingModelName", "model");
    attrs.put("embeddingModelDimension", 64);
    MultimodalEmbeddingResponseAttributes result = new MultimodalEmbeddingResponseAttributes(attrs);
    assertThat(result.getMimeType()).isNull();
    assertThat(result.getMediaType()).isNull();
  }

  @Test
  void constructor_removesFieldsFromMap() {
    HashMap<String, Object> attrs = new HashMap<>();
    attrs.put("embeddingModelName", "model");
    attrs.put("embeddingModelDimension", 64);
    attrs.put("mimeType", "text/plain");
    attrs.put("mediaType", "text");
    attrs.put("custom", "value");
    MultimodalEmbeddingResponseAttributes result = new MultimodalEmbeddingResponseAttributes(attrs);
    assertThat(result.getOtherAttributes()).containsKey("custom");
    assertThat(result.getOtherAttributes()).doesNotContainKey("embeddingModelName");
    assertThat(result.getOtherAttributes()).doesNotContainKey("mimeType");
    assertThat(result.getOtherAttributes()).doesNotContainKey("mediaType");
  }
}
