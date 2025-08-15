package org.mule.extension.vectors.api.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmbeddingResponseAttributes Tests")
class EmbeddingResponseAttributesTest {

  @Test
  @DisplayName("Should create EmbeddingResponseAttributes with all fields")
  void shouldCreateEmbeddingResponseAttributesWithAllFields() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelName", "text-embedding-ada-002");
    attributes.put("embeddingModelDimension", 1536);
    attributes.put("tokenUsage", new TokenUsage(10, 20, 30));
    attributes.put("extra", "value");

    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    assertThat(response.getEmbeddingModelName()).isEqualTo("text-embedding-ada-002");
    assertThat(response.getEmbeddingModelDimension()).isEqualTo(1536);
    assertThat(response.getTokenUsage()).isEqualTo(new TokenUsage(10, 20, 30));
    assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
  }

  @Test
  @DisplayName("Should create EmbeddingResponseAttributes with null values")
  void shouldCreateEmbeddingResponseAttributesWithNullValues() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelDimension", 0); // Required to avoid NPE
    attributes.put("extra", "value");

    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    assertThat(response.getEmbeddingModelName()).isNull();
    assertThat(response.getEmbeddingModelDimension()).isEqualTo(0);
    assertThat(response.getTokenUsage()).isNull();
    assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
  }

  @Test
  @DisplayName("Should create EmbeddingResponseAttributes with empty map")
  void shouldCreateEmbeddingResponseAttributesWithEmptyMap() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelDimension", 0); // Required to avoid NPE

    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    assertThat(response.getEmbeddingModelName()).isNull();
    assertThat(response.getEmbeddingModelDimension()).isEqualTo(0);
    assertThat(response.getTokenUsage()).isNull();
    assertThat(response.getOtherAttributes()).isEmpty();
  }

  @Test
  @DisplayName("Equals should return true for same object")
  void equalsShouldReturnTrueForSameObject() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelName", "text-embedding-ada-002");
    attributes.put("embeddingModelDimension", 1536);
    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    assertThat(response.equals(response)).isTrue();
  }

  @Test
  @DisplayName("Equals should return false for null")
  void equalsShouldReturnFalseForNull() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelName", "text-embedding-ada-002");
    attributes.put("embeddingModelDimension", 1536);
    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    assertThat(response.equals(null)).isFalse();
  }

  @Test
  @DisplayName("Equals should return false for different class")
  void equalsShouldReturnFalseForDifferentClass() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelName", "text-embedding-ada-002");
    attributes.put("embeddingModelDimension", 1536);
    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    assertThat(response.equals("string")).isFalse();
  }

  @Test
  @DisplayName("Equals should return true for identical objects")
  void equalsShouldReturnTrueForIdenticalObjects() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);
    attributes1.put("tokenUsage", new TokenUsage(10, 20, 30));
    attributes1.put("extra", "value");

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);
    attributes2.put("tokenUsage", new TokenUsage(10, 20, 30));
    attributes2.put("extra", "value");

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isTrue();
    assertThat(response2.equals(response1)).isTrue();
  }

  @Test
  @DisplayName("Equals should return false when embeddingModelName differs")
  void equalsShouldReturnFalseWhenEmbeddingModelNameDiffers() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-001");
    attributes2.put("embeddingModelDimension", 1536);

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should return false when embeddingModelDimension differs")
  void equalsShouldReturnFalseWhenEmbeddingModelDimensionDiffers() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1024);

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should return false when tokenUsage differs")
  void equalsShouldReturnFalseWhenTokenUsageDiffers() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);
    attributes1.put("tokenUsage", new TokenUsage(10, 20, 30));

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);
    attributes2.put("tokenUsage", new TokenUsage(15, 25, 40));

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should return false when otherAttributes differs")
  void equalsShouldReturnFalseWhenOtherAttributesDiffers() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);
    attributes1.put("extra", "value1");

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);
    attributes2.put("extra", "value2");

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should handle null embeddingModelName correctly")
  void equalsShouldHandleNullEmbeddingModelNameCorrectly() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should handle null tokenUsage correctly")
  void equalsShouldHandleNullTokenUsageCorrectly() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);
    attributes2.put("tokenUsage", new TokenUsage(10, 20, 30));

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should handle null otherAttributes correctly")
  void equalsShouldHandleNullOtherAttributesCorrectly() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);
    attributes2.put("extra", "value");

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("HashCode should be consistent")
  void hashCodeShouldBeConsistent() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelName", "text-embedding-ada-002");
    attributes.put("embeddingModelDimension", 1536);
    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    int hashCode1 = response.hashCode();
    int hashCode2 = response.hashCode();

    assertThat(hashCode1).isEqualTo(hashCode2);
  }

  @Test
  @DisplayName("HashCode should be equal for equal objects")
  void hashCodeShouldBeEqualForEqualObjects() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
  }

  @Test
  @DisplayName("HashCode should be different for different objects")
  void hashCodeShouldBeDifferentForDifferentObjects() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-001");
    attributes2.put("embeddingModelDimension", 1536);

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.hashCode()).isNotEqualTo(response2.hashCode());
  }

  @Test
  @DisplayName("HashCode should work with null values")
  void hashCodeShouldWorkWithNullValues() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelDimension", 0);
    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    // Expected: result = 0; result = 31 * result + 0; result = 31 * result + 0; result = 31 * result + 0
    int expectedHashCode = 0;
    expectedHashCode = 31 * expectedHashCode + 0; // embeddingModelDimension
    expectedHashCode = 31 * expectedHashCode + 0; // tokenUsage null
    expectedHashCode = 31 * expectedHashCode + 0; // otherAttributes null

    assertThat(response.hashCode()).isEqualTo(expectedHashCode);
  }

  @Test
  @DisplayName("HashCode should work with all null values")
  void hashCodeShouldWorkWithAllNullValues() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelDimension", 0);
    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    assertThat(response.hashCode()).isEqualTo(0);
  }

  @Test
  @DisplayName("HashCode should work with TokenUsage object")
  void hashCodeShouldWorkWithTokenUsageObject() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelName", "text-embedding-ada-002");
    attributes.put("embeddingModelDimension", 1536);
    attributes.put("tokenUsage", new TokenUsage(10, 20, 30));

    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    int expectedHashCode = "text-embedding-ada-002".hashCode();
    expectedHashCode = 31 * expectedHashCode + 1536; // embeddingModelDimension
    expectedHashCode = 31 * expectedHashCode + new TokenUsage(10, 20, 30).hashCode(); // tokenUsage
    expectedHashCode = 31 * expectedHashCode + 0; // otherAttributes null

    assertThat(response.hashCode()).isEqualTo(expectedHashCode);
  }

  @Test
  @DisplayName("Equals should handle case where this.embeddingModelName is null but that.embeddingModelName is not null")
  void equalsShouldHandleThisEmbeddingModelNameNullButThatEmbeddingModelNameNotNull() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should handle case where this.tokenUsage is null but that.tokenUsage is not null")
  void equalsShouldHandleThisTokenUsageNullButThatTokenUsageNotNull() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);
    attributes2.put("tokenUsage", new TokenUsage(10, 20, 30));

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should handle case where this.otherAttributes is null but that.otherAttributes is not null")
  void equalsShouldHandleThisOtherAttributesNullButThatOtherAttributesNotNull() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);
    attributes2.put("extra", "value");

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should handle case where that.embeddingModelName is null but this.embeddingModelName is not null")
  void equalsShouldHandleThatEmbeddingModelNameNullButThisEmbeddingModelNameNotNull() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelDimension", 1536);

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should handle case where that.tokenUsage is null but this.tokenUsage is not null")
  void equalsShouldHandleThatTokenUsageNullButThisTokenUsageNotNull() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);
    attributes1.put("tokenUsage", new TokenUsage(10, 20, 30));

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("Equals should handle case where that.otherAttributes is null but this.otherAttributes is not null")
  void equalsShouldHandleThatOtherAttributesNullButThisOtherAttributesNotNull() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("embeddingModelName", "text-embedding-ada-002");
    attributes1.put("embeddingModelDimension", 1536);
    attributes1.put("extra", "value");

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("embeddingModelName", "text-embedding-ada-002");
    attributes2.put("embeddingModelDimension", 1536);

    EmbeddingResponseAttributes response1 = new EmbeddingResponseAttributes(attributes1);
    EmbeddingResponseAttributes response2 = new EmbeddingResponseAttributes(attributes2);

    assertThat(response1.equals(response2)).isFalse();
    assertThat(response2.equals(response1)).isFalse();
  }

  @Test
  @DisplayName("HashCode should work with mixed null and non-null values")
  void hashCodeShouldWorkWithMixedNullAndNonNullValues() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelName", "text-embedding-ada-002");
    attributes.put("embeddingModelDimension", 1536);
    // tokenUsage and otherAttributes will be null

    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    int expectedHashCode = "text-embedding-ada-002".hashCode();
    expectedHashCode = 31 * expectedHashCode + 1536; // embeddingModelDimension
    expectedHashCode = 31 * expectedHashCode + 0; // tokenUsage null
    expectedHashCode = 31 * expectedHashCode + 0; // otherAttributes null

    assertThat(response.hashCode()).isEqualTo(expectedHashCode);
  }

  @Test
  @DisplayName("HashCode should work with all non-null values")
  void hashCodeShouldWorkWithAllNonNullValues() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("embeddingModelName", "text-embedding-ada-002");
    attributes.put("embeddingModelDimension", 1536);
    attributes.put("tokenUsage", new TokenUsage(10, 20, 30));
    attributes.put("extra", "value");

    EmbeddingResponseAttributes response = new EmbeddingResponseAttributes(attributes);

    int expectedHashCode = "text-embedding-ada-002".hashCode();
    expectedHashCode = 31 * expectedHashCode + 1536; // embeddingModelDimension
    expectedHashCode = 31 * expectedHashCode + new TokenUsage(10, 20, 30).hashCode(); // tokenUsage
    expectedHashCode = 31 * expectedHashCode + response.getOtherAttributes().hashCode(); // otherAttributes

    assertThat(response.hashCode()).isEqualTo(expectedHashCode);
  }
}
