package org.mule.extension.vectors.api.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.stream.Stream;

@DisplayName("MultimodalEmbeddingResponseAttributes Tests")
class MultimodalEmbeddingResponseAttributesTest {

    @Test
    @DisplayName("Should create MultimodalEmbeddingResponseAttributes with all fields")
    void shouldCreateMultimodalEmbeddingResponseAttributesWithAllFields() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelName", "text-embedding-ada-002");
        attributes.put("embeddingModelDimension", 1536);
        attributes.put("tokenUsage", new TokenUsage(10, 20, 30));
        attributes.put("mimeType", "image/jpeg");
        attributes.put("mediaType", "image");
        attributes.put("extra", "value");

        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

        assertThat(response.getEmbeddingModelName()).isEqualTo("text-embedding-ada-002");
        assertThat(response.getEmbeddingModelDimension()).isEqualTo(1536);
        assertThat(response.getTokenUsage()).isEqualTo(new TokenUsage(10, 20, 30));
        assertThat(response.getMimeType()).isEqualTo("image/jpeg");
        assertThat(response.getMediaType()).isEqualTo("image");
        assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
    }

    @Test
    @DisplayName("Should create MultimodalEmbeddingResponseAttributes with null values")
    void shouldCreateMultimodalEmbeddingResponseAttributesWithNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelName", "text-embedding-ada-002");
        attributes.put("embeddingModelDimension", 1536);
        attributes.put("extra", "value");

        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

        assertThat(response.getEmbeddingModelName()).isEqualTo("text-embedding-ada-002");
        assertThat(response.getMimeType()).isNull();
        assertThat(response.getMediaType()).isNull();
        assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
    }

    @Test
    @DisplayName("Should create MultimodalEmbeddingResponseAttributes with empty map")
    void shouldCreateMultimodalEmbeddingResponseAttributesWithEmptyMap() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelDimension", 0);

        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

        assertThat(response.getEmbeddingModelName()).isNull();
        assertThat(response.getMimeType()).isNull();
        assertThat(response.getMediaType()).isNull();
        assertThat(response.getOtherAttributes()).isEmpty();
    }

    @Test
    @DisplayName("Equals should return true for same object")
    void equalsShouldReturnTrueForSameObject() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelName", "text-embedding-ada-002");
        attributes.put("embeddingModelDimension", 1536);
        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

        assertThat(response.equals(response)).isTrue();
    }

    @Test
    @DisplayName("Equals should return false for null")
    void equalsShouldReturnFalseForNull() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelName", "text-embedding-ada-002");
        attributes.put("embeddingModelDimension", 1536);
        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

        assertThat(response.equals(null)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false for different class")
    void equalsShouldReturnFalseForDifferentClass() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelName", "text-embedding-ada-002");
        attributes.put("embeddingModelDimension", 1536);
        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

        assertThat(response.equals("string")).isFalse();
    }

    @Test
    @DisplayName("Equals should return false for parent class")
    void equalsShouldReturnFalseForParentClass() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("embeddingModelName", "text-embedding-ada-002");
        attributes1.put("embeddingModelDimension", 1536);
        MultimodalEmbeddingResponseAttributes multimodalResponse = new MultimodalEmbeddingResponseAttributes(attributes1);

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("embeddingModelName", "text-embedding-ada-002");
        attributes2.put("embeddingModelDimension", 1536);
        EmbeddingResponseAttributes parentResponse = new EmbeddingResponseAttributes(attributes2);

        assertThat(multimodalResponse.equals(parentResponse)).isFalse();
        assertThat(parentResponse.equals(multimodalResponse)).isFalse();
    }

    @Test
    @DisplayName("Equals should return true for identical objects")
    void equalsShouldReturnTrueForIdenticalObjects() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("embeddingModelName", "text-embedding-ada-002");
        attributes1.put("embeddingModelDimension", 1536);
        attributes1.put("tokenUsage", new TokenUsage(10, 20, 30));
        attributes1.put("mimeType", "image/jpeg");
        attributes1.put("mediaType", "image");
        attributes1.put("extra", "value");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("embeddingModelName", "text-embedding-ada-002");
        attributes2.put("embeddingModelDimension", 1536);
        attributes2.put("tokenUsage", new TokenUsage(10, 20, 30));
        attributes2.put("mimeType", "image/jpeg");
        attributes2.put("mediaType", "image");
        attributes2.put("extra", "value");

        MultimodalEmbeddingResponseAttributes response1 = new MultimodalEmbeddingResponseAttributes(attributes1);
        MultimodalEmbeddingResponseAttributes response2 = new MultimodalEmbeddingResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isTrue();
        assertThat(response2.equals(response1)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideDifferentFieldValues")
    @DisplayName("Equals should return false when specific fields differ")
    void equalsShouldReturnFalseWhenFieldsDiffer(String testCase, HashMap<String, Object> attributes1, HashMap<String, Object> attributes2) {
        MultimodalEmbeddingResponseAttributes response1 = new MultimodalEmbeddingResponseAttributes(attributes1);
        MultimodalEmbeddingResponseAttributes response2 = new MultimodalEmbeddingResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    private static Stream<Arguments> provideDifferentFieldValues() {
        return Stream.of(
            Arguments.of("mimeType differs", 
                createAttributes("text-embedding-ada-002", 1536, "image/jpeg", "image"),
                createAttributes("text-embedding-ada-002", 1536, "image/png", "image")),
            Arguments.of("mediaType differs", 
                createAttributes("text-embedding-ada-002", 1536, "image/jpeg", "image"),
                createAttributes("text-embedding-ada-002", 1536, "image/jpeg", "text")),
            Arguments.of("parent class field differs", 
                createAttributes("text-embedding-ada-002", 1536, "image/jpeg", "image"),
                createAttributes("text-embedding-ada-001", 1536, "image/jpeg", "image"))
        );
    }

    private static HashMap<String, Object> createAttributes(String modelName, int dimension, String mimeType, String mediaType) {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelName", modelName);
        attributes.put("embeddingModelDimension", dimension);
        attributes.put("mimeType", mimeType);
        attributes.put("mediaType", mediaType);
        return attributes;
    }

    @Test
    @DisplayName("HashCode should be consistent")
    void hashCodeShouldBeConsistent() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelName", "text-embedding-ada-002");
        attributes.put("embeddingModelDimension", 1536);
        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

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
        attributes1.put("mimeType", "image/jpeg");
        attributes1.put("mediaType", "image");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("embeddingModelName", "text-embedding-ada-002");
        attributes2.put("embeddingModelDimension", 1536);
        attributes2.put("mimeType", "image/jpeg");
        attributes2.put("mediaType", "image");

        MultimodalEmbeddingResponseAttributes response1 = new MultimodalEmbeddingResponseAttributes(attributes1);
        MultimodalEmbeddingResponseAttributes response2 = new MultimodalEmbeddingResponseAttributes(attributes2);

        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("HashCode should be different for different objects")
    void hashCodeShouldBeDifferentForDifferentObjects() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("embeddingModelName", "text-embedding-ada-002");
        attributes1.put("embeddingModelDimension", 1536);
        attributes1.put("mimeType", "image/jpeg");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("embeddingModelName", "text-embedding-ada-002");
        attributes2.put("embeddingModelDimension", 1536);
        attributes2.put("mimeType", "image/png");

        MultimodalEmbeddingResponseAttributes response1 = new MultimodalEmbeddingResponseAttributes(attributes1);
        MultimodalEmbeddingResponseAttributes response2 = new MultimodalEmbeddingResponseAttributes(attributes2);

        assertThat(response1.hashCode()).isNotEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("HashCode should work with null values")
    void hashCodeShouldWorkWithNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelDimension", 0);
        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

        // Expected: parent hashCode + 31 * 0 + 31 * 0 (for null mimeType and mediaType)
        int parentHashCode = 0; // parent class hashCode with all null values
        int expectedHashCode = parentHashCode;
        expectedHashCode = 31 * expectedHashCode + 0; // mimeType null
        expectedHashCode = 31 * expectedHashCode + 0; // mediaType null

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }

    @Test
    @DisplayName("HashCode should work with all null values")
    void hashCodeShouldWorkWithAllNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelDimension", 0);
        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

        // Expected: super.hashCode() + 0 + 0 (for null mimeType and mediaType)
        int expectedHashCode = 0; // super.hashCode() with all null values
        expectedHashCode = 31 * expectedHashCode + 0; // mimeType null
        expectedHashCode = 31 * expectedHashCode + 0; // mediaType null

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }

    @Test
    @DisplayName("Equals should handle case where this.mimeType is null but that.mimeType is not null")
    void equalsShouldHandleThisMimeTypeNullButThatMimeTypeNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("embeddingModelDimension", 1536);
        // No mimeType in attributes1

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("embeddingModelDimension", 1536);
        attributes2.put("mimeType", "image/jpeg");

        MultimodalEmbeddingResponseAttributes response1 = new MultimodalEmbeddingResponseAttributes(attributes1);
        MultimodalEmbeddingResponseAttributes response2 = new MultimodalEmbeddingResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where this.mediaType is null but that.mediaType is not null")
    void equalsShouldHandleThisMediaTypeNullButThatMediaTypeNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("embeddingModelDimension", 1536);
        // No mediaType in attributes1

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("embeddingModelDimension", 1536);
        attributes2.put("mediaType", "image");

        MultimodalEmbeddingResponseAttributes response1 = new MultimodalEmbeddingResponseAttributes(attributes1);
        MultimodalEmbeddingResponseAttributes response2 = new MultimodalEmbeddingResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where that.mimeType is null but this.mimeType is not null")
    void equalsShouldHandleThatMimeTypeNullButThisMimeTypeNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("embeddingModelDimension", 1536);
        attributes1.put("mimeType", "image/jpeg");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("embeddingModelDimension", 1536);
        // No mimeType in attributes2

        MultimodalEmbeddingResponseAttributes response1 = new MultimodalEmbeddingResponseAttributes(attributes1);
        MultimodalEmbeddingResponseAttributes response2 = new MultimodalEmbeddingResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where that.mediaType is null but this.mediaType is not null")
    void equalsShouldHandleThatMediaTypeNullButThisMediaTypeNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("embeddingModelDimension", 1536);
        attributes1.put("mediaType", "image");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("embeddingModelDimension", 1536);
        // No mediaType in attributes2

        MultimodalEmbeddingResponseAttributes response1 = new MultimodalEmbeddingResponseAttributes(attributes1);
        MultimodalEmbeddingResponseAttributes response2 = new MultimodalEmbeddingResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("HashCode should work with mixed null and non-null values")
    void hashCodeShouldWorkWithMixedNullAndNonNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelDimension", 1536);
        attributes.put("mimeType", "image/jpeg");
        // mediaType will be null

        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

        // Instead of manual calculation, verify consistency and non-zero
        assertThat(response.hashCode()).isNotEqualTo(0);
        
        // Create another identical object and verify they have the same hashCode
        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("embeddingModelDimension", 1536);
        attributes2.put("mimeType", "image/jpeg");
        
        MultimodalEmbeddingResponseAttributes response2 = new MultimodalEmbeddingResponseAttributes(attributes2);
        assertThat(response.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("HashCode should work with all non-null values")
    void hashCodeShouldWorkWithAllNonNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("embeddingModelDimension", 1536);
        attributes.put("mimeType", "image/jpeg");
        attributes.put("mediaType", "image");

        MultimodalEmbeddingResponseAttributes response = new MultimodalEmbeddingResponseAttributes(attributes);

        // Instead of manual calculation, verify consistency and non-zero
        assertThat(response.hashCode()).isNotEqualTo(0);
        
        // Create another identical object and verify they have the same hashCode
        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("embeddingModelDimension", 1536);
        attributes2.put("mimeType", "image/jpeg");
        attributes2.put("mediaType", "image");
        
        MultimodalEmbeddingResponseAttributes response2 = new MultimodalEmbeddingResponseAttributes(attributes2);
        assertThat(response.hashCode()).isEqualTo(response2.hashCode());
    }
} 
