package org.mule.extension.vectors.api.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.stream.Stream;

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

    @ParameterizedTest
    @MethodSource("provideThisNullFieldScenarios")
    @DisplayName("Equals should handle this null field scenarios")
    void equalsShouldHandleThisNullFieldScenarios(String testCase, HashMap<String, Object> attributes1, HashMap<String, Object> attributes2) {
        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    private static Stream<Arguments> provideThisNullFieldScenarios() {
        return Stream.of(
            Arguments.of("this.fileType is null but that.fileType is not null",
                createAttributes(null, "document", "application/pdf", "extra"),
                createAttributes("pdf", "document", "application/pdf", "extra")),
            Arguments.of("this.mediaType is null but that.mediaType is not null",
                createAttributes("pdf", null, "application/pdf", "extra"),
                createAttributes("pdf", "document", "application/pdf", "extra")),
            Arguments.of("this.mimeType is null but that.mimeType is not null",
                createAttributes("pdf", "document", null, "extra"),
                createAttributes("pdf", "document", "application/pdf", "extra")),
            Arguments.of("this.otherAttributes is null but that.otherAttributes is not null",
                createAttributes("pdf", "document", "application/pdf", null),
                createAttributes("pdf", "document", "application/pdf", "extra"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideThatNullFieldScenarios")
    @DisplayName("Equals should handle that null field scenarios")
    void equalsShouldHandleThatNullFieldScenarios(String testCase, HashMap<String, Object> attributes1, HashMap<String, Object> attributes2) {
        TransformResponseAttributes response1 = new TransformResponseAttributes(attributes1);
        TransformResponseAttributes response2 = new TransformResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    private static Stream<Arguments> provideThatNullFieldScenarios() {
        return Stream.of(
            Arguments.of("that.fileType is null but this.fileType is not null",
                createAttributes("pdf", "document", "application/pdf", "extra"),
                createAttributes(null, "document", "application/pdf", "extra")),
            Arguments.of("that.mediaType is null but this.mediaType is not null",
                createAttributes("pdf", "document", "application/pdf", "extra"),
                createAttributes("pdf", null, "application/pdf", "extra")),
            Arguments.of("that.mimeType is null but this.mimeType is not null",
                createAttributes("pdf", "document", "application/pdf", "extra"),
                createAttributes("pdf", "document", null, "extra")),
            Arguments.of("that.otherAttributes is null but this.otherAttributes is not null",
                createAttributes("pdf", "document", "application/pdf", "extra"),
                createAttributes("pdf", "document", "application/pdf", null))
        );
    }

    private static HashMap<String, Object> createAttributes(String fileType, String mediaType, String mimeType, String extra) {
        HashMap<String, Object> attributes = new HashMap<>();
        if (fileType != null) attributes.put("fileType", fileType);
        if (mediaType != null) attributes.put("mediaType", mediaType);
        if (mimeType != null) attributes.put("mimeType", mimeType);
        if (extra != null) attributes.put("extra", extra);
        return attributes;
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
