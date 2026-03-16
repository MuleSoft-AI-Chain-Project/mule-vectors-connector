package org.mule.extension.vectors.internal.metadata;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.api.metadata.ChunkResponseAttributes;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

class ChunkResponseAttributesTest {

  @Test
  void constructor_extractsMaxSegmentSize() {
    HashMap<String, Object> attrs = new HashMap<>();
    attrs.put("maxSegmentSizeInChars", 500);
    attrs.put("maxOverlapSizeInChars", 50);
    ChunkResponseAttributes result = new ChunkResponseAttributes(attrs);
    assertThat(result.getMaxSegmentSizeInChars()).isEqualTo(500);
    assertThat(result.getMaxOverlapSizeInChars()).isEqualTo(50);
  }

  @Test
  void constructor_handlesNullValues() {
    HashMap<String, Object> attrs = new HashMap<>();
    ChunkResponseAttributes result = new ChunkResponseAttributes(attrs);
    assertThat(result.getMaxSegmentSizeInChars()).isNull();
    assertThat(result.getMaxOverlapSizeInChars()).isNull();
  }

  @Test
  void constructor_extractsOnlySegmentSize() {
    HashMap<String, Object> attrs = new HashMap<>();
    attrs.put("maxSegmentSizeInChars", 1000);
    ChunkResponseAttributes result = new ChunkResponseAttributes(attrs);
    assertThat(result.getMaxSegmentSizeInChars()).isEqualTo(1000);
    assertThat(result.getMaxOverlapSizeInChars()).isNull();
  }

  @Test
  void equals_sameObject() {
    HashMap<String, Object> attrs = new HashMap<>();
    attrs.put("maxSegmentSizeInChars", 500);
    ChunkResponseAttributes a = new ChunkResponseAttributes(attrs);
    assertThat(a.equals(a)).isTrue();
  }

  @Test
  void equals_equalObjects() {
    HashMap<String, Object> attrs1 = new HashMap<>();
    attrs1.put("maxSegmentSizeInChars", 500);
    attrs1.put("maxOverlapSizeInChars", 50);
    HashMap<String, Object> attrs2 = new HashMap<>();
    attrs2.put("maxSegmentSizeInChars", 500);
    attrs2.put("maxOverlapSizeInChars", 50);
    assertThat(new ChunkResponseAttributes(attrs1)).isEqualTo(new ChunkResponseAttributes(attrs2));
  }

  @Test
  void equals_differentObjects() {
    HashMap<String, Object> attrs1 = new HashMap<>();
    attrs1.put("maxSegmentSizeInChars", 500);
    HashMap<String, Object> attrs2 = new HashMap<>();
    attrs2.put("maxSegmentSizeInChars", 600);
    assertThat(new ChunkResponseAttributes(attrs1)).isNotEqualTo(new ChunkResponseAttributes(attrs2));
  }

  @Test
  void equals_null() {
    ChunkResponseAttributes a = new ChunkResponseAttributes(new HashMap<>());
    assertThat(a.equals(null)).isFalse();
  }

  @Test
  void equals_differentType() {
    ChunkResponseAttributes a = new ChunkResponseAttributes(new HashMap<>());
    assertThat(a.equals("string")).isFalse();
  }

  @Test
  void equals_nullSegmentSizeVsNonNull() {
    HashMap<String, Object> attrs1 = new HashMap<>();
    HashMap<String, Object> attrs2 = new HashMap<>();
    attrs2.put("maxSegmentSizeInChars", 100);
    assertThat(new ChunkResponseAttributes(attrs1)).isNotEqualTo(new ChunkResponseAttributes(attrs2));
  }

  @Test
  void equals_nullOverlapVsNonNull() {
    HashMap<String, Object> attrs1 = new HashMap<>();
    attrs1.put("maxSegmentSizeInChars", 100);
    HashMap<String, Object> attrs2 = new HashMap<>();
    attrs2.put("maxSegmentSizeInChars", 100);
    attrs2.put("maxOverlapSizeInChars", 10);
    assertThat(new ChunkResponseAttributes(attrs1)).isNotEqualTo(new ChunkResponseAttributes(attrs2));
  }

  @Test
  void hashCode_consistent() {
    HashMap<String, Object> attrs = new HashMap<>();
    attrs.put("maxSegmentSizeInChars", 500);
    attrs.put("maxOverlapSizeInChars", 50);
    ChunkResponseAttributes a = new ChunkResponseAttributes(attrs);
    assertThat(a.hashCode()).isEqualTo(a.hashCode());
  }

  @Test
  void hashCode_nullFields() {
    ChunkResponseAttributes a = new ChunkResponseAttributes(new HashMap<>());
    assertThat(a.hashCode()).isEqualTo(0);
  }
}
