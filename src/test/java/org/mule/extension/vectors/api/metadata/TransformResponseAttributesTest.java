package org.mule.extension.vectors.api.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

@DisplayName("TransformResponseAttributes Tests")
class TransformResponseAttributesTest {

    @Test
    @DisplayName("Should create TransformResponseAttributes with all fields")
    void shouldCreateTransformResponseAttributesWithAllFields() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("fileType", "pdf");
        attributes.put("mediaType", "document");
        attributes.put("mimeType", "application/pdf");
        attributes.put("extra", "value");

        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        assertThat(response.getFileType()).isEqualTo("pdf");
        assertThat(response.getMediaType()).isEqualTo("document");
        assertThat(response.getMimeType()).isEqualTo("application/pdf");
        assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
    }

    @Test
    @DisplayName("Should create TransformResponseAttributes with null values")
    void shouldCreateTransformResponseAttributesWithNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("extra", "value");

        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        assertThat(response.getFileType()).isNull();
        assertThat(response.getMediaType()).isNull();
        assertThat(response.getMimeType()).isNull();
        assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
    }

    @Test
    @DisplayName("Should create TransformResponseAttributes with empty map")
    void shouldCreateTransformResponseAttributesWithEmptyMap() {
        HashMap<String, Object> attributes = new HashMap<>();

        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        assertThat(response.getFileType()).isNull();
        assertThat(response.getMediaType()).isNull();
        assertThat(response.getMimeType()).isNull();
        assertThat(response.getOtherAttributes()).isEmpty();
    }

    @Test
    @DisplayName("Equals should return true for same object")
    void equalsShouldReturnTrueForSameObject() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("fileType", "pdf");
        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        assertThat(response.equals(response)).isTrue();
    }

    @Test
    @DisplayName("Equals should return false for null")
    void equalsShouldReturnFalseForNull() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("fileType", "pdf");
        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        assertThat(response.equals(null)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false for different class")
    void equalsShouldReturnFalseForDifferentClass() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("fileType", "pdf");
        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        assertThat(response.equals("string")).isFalse();
    }

    @Test
    @DisplayName("Equals should return true for identical objects")
    void equalsShouldReturnTrueForIdenticalObjects() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");
        attributes1.put("mediaType", "document");
        attributes1.put("mimeType", "application/pdf");
        attributes1.put("extra", "value");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "pdf");
        attributes2.put("mediaType", "document");
        attributes2.put("mimeType", "application/pdf");
        attributes2.put("extra", "value");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isTrue();
        assertThat(response2.equals(response1)).isTrue();
    }

    @Test
    @DisplayName("Equals should return false when fileType differs")
    void equalsShouldReturnFalseWhenFileTypeDiffers() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");
        attributes1.put("mediaType", "document");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "doc");
        attributes2.put("mediaType", "document");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false when mediaType differs")
    void equalsShouldReturnFalseWhenMediaTypeDiffers() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");
        attributes1.put("mediaType", "document");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "pdf");
        attributes2.put("mediaType", "image");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false when mimeType differs")
    void equalsShouldReturnFalseWhenMimeTypeDiffers() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");
        attributes1.put("mimeType", "application/pdf");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "pdf");
        attributes2.put("mimeType", "application/doc");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false when otherAttributes differs")
    void equalsShouldReturnFalseWhenOtherAttributesDiffers() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");
        attributes1.put("extra", "value1");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "pdf");
        attributes2.put("extra", "value2");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle null fileType correctly")
    void equalsShouldHandleNullFileTypeCorrectly() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("mediaType", "document");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "pdf");
        attributes2.put("mediaType", "document");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle null mediaType correctly")
    void equalsShouldHandleNullMediaTypeCorrectly() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "pdf");
        attributes2.put("mediaType", "document");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle null mimeType correctly")
    void equalsShouldHandleNullMimeTypeCorrectly() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "pdf");
        attributes2.put("mimeType", "application/pdf");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle null otherAttributes correctly")
    void equalsShouldHandleNullOtherAttributesCorrectly() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "pdf");
        attributes2.put("extra", "value");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("HashCode should be consistent")
    void hashCodeShouldBeConsistent() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("fileType", "pdf");
        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        int hashCode1 = response.hashCode();
        int hashCode2 = response.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("HashCode should be equal for equal objects")
    void hashCodeShouldBeEqualForEqualObjects() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");
        attributes1.put("mediaType", "document");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "pdf");
        attributes2.put("mediaType", "document");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("HashCode should be different for different objects")
    void hashCodeShouldBeDifferentForDifferentObjects() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "doc");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.hashCode()).isNotEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("HashCode should work with null values")
    void hashCodeShouldWorkWithNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        // Expected: result = 0; result = 31 * result + 0; result = 31 * result + 0; result = 31 * result + 0
        int expectedHashCode = 0;
        expectedHashCode = 31 * expectedHashCode + 0; // mediaType null
        expectedHashCode = 31 * expectedHashCode + 0; // mimeType null
        expectedHashCode = 31 * expectedHashCode + 0; // otherAttributes null

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }

    @Test
    @DisplayName("HashCode should work with all null values")
    void hashCodeShouldWorkWithAllNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        assertThat(response.hashCode()).isEqualTo(0);
    }

    @Test
    @DisplayName("Equals should handle case where this.fileType is null but that.fileType is not null")
    void equalsShouldHandleThisFileTypeNullButThatFileTypeNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        // No fileType in attributes1

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("fileType", "pdf");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where this.mediaType is null but that.mediaType is not null")
    void equalsShouldHandleThisMediaTypeNullButThatMediaTypeNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        // No mediaType in attributes1

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("mediaType", "document");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where this.mimeType is null but that.mimeType is not null")
    void equalsShouldHandleThisMimeTypeNullButThatMimeTypeNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        // No mimeType in attributes1

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("mimeType", "application/pdf");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where this.otherAttributes is null but that.otherAttributes is not null")
    void equalsShouldHandleThisOtherAttributesNullButThatOtherAttributesNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        // No otherAttributes in attributes1

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("extra", "value");

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where that.fileType is null but this.fileType is not null")
    void equalsShouldHandleThatFileTypeNullButThisFileTypeNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("fileType", "pdf");

        HashMap<String, Object> attributes2 = new HashMap<>();
        // No fileType in attributes2

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where that.mediaType is null but this.mediaType is not null")
    void equalsShouldHandleThatMediaTypeNullButThisMediaTypeNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("mediaType", "document");

        HashMap<String, Object> attributes2 = new HashMap<>();
        // No mediaType in attributes2

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where that.mimeType is null but this.mimeType is not null")
    void equalsShouldHandleThatMimeTypeNullButThisMimeTypeNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("mimeType", "application/pdf");

        HashMap<String, Object> attributes2 = new HashMap<>();
        // No mimeType in attributes2

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where that.otherAttributes is null but this.otherAttributes is not null")
    void equalsShouldHandleThatOtherAttributesNullButThisOtherAttributesNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("extra", "value");

        HashMap<String, Object> attributes2 = new HashMap<>();
        // No otherAttributes in attributes2

        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("HashCode should work with mixed null and non-null values")
    void hashCodeShouldWorkWithMixedNullAndNonNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("fileType", "pdf");
        // mediaType, mimeType, and otherAttributes will be null

        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        int expectedHashCode = "pdf".hashCode();
        expectedHashCode = 31 * expectedHashCode + 0; // mediaType null
        expectedHashCode = 31 * expectedHashCode + 0; // mimeType null
        expectedHashCode = 31 * expectedHashCode + 0; // otherAttributes null

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }

    @Test
    @DisplayName("HashCode should work with all non-null values")
    void hashCodeShouldWorkWithAllNonNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("fileType", "pdf");
        attributes.put("mediaType", "document");
        attributes.put("mimeType", "application/pdf");
        attributes.put("extra", "value");

        TransformResponseAttributes response = new TransformResponseAttributes(attributes);

        int expectedHashCode = "pdf".hashCode();
        expectedHashCode = 31 * expectedHashCode + "document".hashCode(); // mediaType
        expectedHashCode = 31 * expectedHashCode + "application/pdf".hashCode(); // mimeType
        expectedHashCode = 31 * expectedHashCode + response.getOtherAttributes().hashCode(); // otherAttributes

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }
} 
