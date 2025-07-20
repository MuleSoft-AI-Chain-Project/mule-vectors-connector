package org.mule.extension.vectors.api.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@DisplayName("StorageResponseAttributes Tests")
class StorageResponseAttributesTest {

    @Test
    @DisplayName("Should create StorageResponseAttributes with all fields")
    void shouldCreateStorageResponseAttributesWithAllFields() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("path", "/path/to/file");
        attributes.put("fileName", "document.pdf");
        attributes.put("mimeType", "application/pdf");
        attributes.put("metadata", createMetadataMap());
        attributes.put("extra", "value");

        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        assertThat(response.getPath()).isEqualTo("/path/to/file");
        assertThat(response.getFileName()).isEqualTo("document.pdf");
        assertThat(response.getMimeType()).isEqualTo("application/pdf");
        assertThat(response.getMetadata()).isEqualTo(createMetadataMap());
        assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
    }

    @Test
    @DisplayName("Should create StorageResponseAttributes with null values")
    void shouldCreateStorageResponseAttributesWithNullValues() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("extra", "value");

        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        assertThat(response.getPath()).isNull();
        assertThat(response.getFileName()).isNull();
        assertThat(response.getMimeType()).isNull();
        assertThat(response.getMetadata()).isNull();
        assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
    }

    @Test
    @DisplayName("Should create StorageResponseAttributes with empty map")
    void shouldCreateStorageResponseAttributesWithEmptyMap() {
        Map<String, Object> attributes = new HashMap<>();

        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        assertThat(response.getPath()).isNull();
        assertThat(response.getFileName()).isNull();
        assertThat(response.getMimeType()).isNull();
        assertThat(response.getMetadata()).isNull();
        assertThat(response.getOtherAttributes()).isEmpty();
    }

    @Test
    @DisplayName("Equals should return true for same object")
    void equalsShouldReturnTrueForSameObject() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("path", "/path/to/file");
        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        assertThat(response.equals(response)).isTrue();
    }

    @Test
    @DisplayName("Equals should return false for null")
    void equalsShouldReturnFalseForNull() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("path", "/path/to/file");
        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        assertThat(response.equals(null)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false for different class")
    void equalsShouldReturnFalseForDifferentClass() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("path", "/path/to/file");
        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        assertThat(response.equals("string")).isFalse();
    }

    @Test
    @DisplayName("Equals should return true for identical objects")
    void equalsShouldReturnTrueForIdenticalObjects() {
        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put("path", "/path/to/file");
        attributes1.put("fileName", "document.pdf");
        attributes1.put("mimeType", "application/pdf");
        attributes1.put("metadata", createMetadataMap());
        attributes1.put("extra", "value");

        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put("path", "/path/to/file");
        attributes2.put("fileName", "document.pdf");
        attributes2.put("mimeType", "application/pdf");
        attributes2.put("metadata", createMetadataMap());
        attributes2.put("extra", "value");

        StorageResponseAttributes response1 = new StorageResponseAttributes(attributes1);
        StorageResponseAttributes response2 = new StorageResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isTrue();
        assertThat(response2.equals(response1)).isTrue();
    }

    @Test
    @DisplayName("Equals should return false when path differs")
    void equalsShouldReturnFalseWhenPathDiffers() {
        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put("path", "/path/to/file");
        attributes1.put("fileName", "document.pdf");

        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put("path", "/different/path");
        attributes2.put("fileName", "document.pdf");

        StorageResponseAttributes response1 = new StorageResponseAttributes(attributes1);
        StorageResponseAttributes response2 = new StorageResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false when fileName differs")
    void equalsShouldReturnFalseWhenFileNameDiffers() {
        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put("path", "/path/to/file");
        attributes1.put("fileName", "document.pdf");

        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put("path", "/path/to/file");
        attributes2.put("fileName", "document.docx");

        StorageResponseAttributes response1 = new StorageResponseAttributes(attributes1);
        StorageResponseAttributes response2 = new StorageResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false when mimeType differs")
    void equalsShouldReturnFalseWhenMimeTypeDiffers() {
        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put("path", "/path/to/file");
        attributes1.put("mimeType", "application/pdf");

        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put("path", "/path/to/file");
        attributes2.put("mimeType", "application/doc");

        StorageResponseAttributes response1 = new StorageResponseAttributes(attributes1);
        StorageResponseAttributes response2 = new StorageResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false when metadata differs")
    void equalsShouldReturnFalseWhenMetadataDiffers() {
        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put("path", "/path/to/file");
        attributes1.put("metadata", createMetadataMap());

        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put("path", "/path/to/file");
        attributes2.put("metadata", createDifferentMetadataMap());

        StorageResponseAttributes response1 = new StorageResponseAttributes(attributes1);
        StorageResponseAttributes response2 = new StorageResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false when otherAttributes differs")
    void equalsShouldReturnFalseWhenOtherAttributesDiffers() {
        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put("path", "/path/to/file");
        attributes1.put("extra", "value1");

        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put("path", "/path/to/file");
        attributes2.put("extra", "value2");

        StorageResponseAttributes response1 = new StorageResponseAttributes(attributes1);
        StorageResponseAttributes response2 = new StorageResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideNullFieldScenarios")
    @DisplayName("Equals should handle null field scenarios")
    void equalsShouldHandleNullFieldScenarios(String testCase, Map<String, Object> attributes1, Map<String, Object> attributes2) {
        StorageResponseAttributes response1 = new StorageResponseAttributes(attributes1);
        StorageResponseAttributes response2 = new StorageResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    private static Stream<Arguments> provideNullFieldScenarios() {
        return Stream.of(
            Arguments.of("this.path is null but that.path is not null",
                createAttributes(null, "test.txt", "text/plain", createMetadataMap(), "extra"),
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), "extra")),
            Arguments.of("this.fileName is null but that.fileName is not null",
                createAttributes("/test/path", null, "text/plain", createMetadataMap(), "extra"),
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), "extra")),
            Arguments.of("this.mimeType is null but that.mimeType is not null",
                createAttributes("/test/path", "test.txt", null, createMetadataMap(), "extra"),
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), "extra")),
            Arguments.of("this.metadata is null but that.metadata is not null",
                createAttributes("/test/path", "test.txt", "text/plain", null, "extra"),
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), "extra")),
            Arguments.of("this.otherAttributes is null but that.otherAttributes is not null",
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), null),
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), "extra"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideThatNullFieldScenarios")
    @DisplayName("Equals should handle that null field scenarios")
    void equalsShouldHandleThatNullFieldScenarios(String testCase, Map<String, Object> attributes1, Map<String, Object> attributes2) {
        StorageResponseAttributes response1 = new StorageResponseAttributes(attributes1);
        StorageResponseAttributes response2 = new StorageResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    private static Stream<Arguments> provideThatNullFieldScenarios() {
        return Stream.of(
            Arguments.of("that.path is null but this.path is not null",
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), "extra"),
                createAttributes(null, "test.txt", "text/plain", createMetadataMap(), "extra")),
            Arguments.of("that.fileName is null but this.fileName is not null",
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), "extra"),
                createAttributes("/test/path", null, "text/plain", createMetadataMap(), "extra")),
            Arguments.of("that.mimeType is null but this.mimeType is not null",
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), "extra"),
                createAttributes("/test/path", "test.txt", null, createMetadataMap(), "extra")),
            Arguments.of("that.metadata is null but this.metadata is not null",
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), "extra"),
                createAttributes("/test/path", "test.txt", "text/plain", null, "extra")),
            Arguments.of("that.otherAttributes is null but this.otherAttributes is not null",
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), "extra"),
                createAttributes("/test/path", "test.txt", "text/plain", createMetadataMap(), null))
        );
    }

    private static Map<String, Object> createAttributes(String path, String fileName, String mimeType, Map<String, Object> metadata, String extra) {
        Map<String, Object> attributes = new HashMap<>();
        if (path != null) attributes.put("path", path);
        if (fileName != null) attributes.put("fileName", fileName);
        if (mimeType != null) attributes.put("mimeType", mimeType);
        if (metadata != null) attributes.put("metadata", metadata);
        if (extra != null) attributes.put("extra", extra);
        return attributes;
    }

    @Test
    @DisplayName("HashCode should be consistent")
    void hashCodeShouldBeConsistent() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("path", "/path/to/file");
        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        int hashCode1 = response.hashCode();
        int hashCode2 = response.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("HashCode should be equal for equal objects")
    void hashCodeShouldBeEqualForEqualObjects() {
        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put("path", "/path/to/file");
        attributes1.put("fileName", "document.pdf");

        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put("path", "/path/to/file");
        attributes2.put("fileName", "document.pdf");

        StorageResponseAttributes response1 = new StorageResponseAttributes(attributes1);
        StorageResponseAttributes response2 = new StorageResponseAttributes(attributes2);

        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("HashCode should be different for different objects")
    void hashCodeShouldBeDifferentForDifferentObjects() {
        Map<String, Object> attributes1 = new HashMap<>();
        attributes1.put("path", "/path/to/file");

        Map<String, Object> attributes2 = new HashMap<>();
        attributes2.put("path", "/different/path");

        StorageResponseAttributes response1 = new StorageResponseAttributes(attributes1);
        StorageResponseAttributes response2 = new StorageResponseAttributes(attributes2);

        assertThat(response1.hashCode()).isNotEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("HashCode should work with null values")
    void hashCodeShouldWorkWithNullValues() {
        Map<String, Object> attributes = new HashMap<>();
        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        // Expected: Objects.hash(null, null, null, null, null)
        int expectedHashCode = java.util.Objects.hash(null, null, null, null, null);

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }

    @Test
    @DisplayName("HashCode should work with all null values")
    void hashCodeShouldWorkWithAllNullValues() {
        Map<String, Object> attributes = new HashMap<>();
        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        assertThat(response.hashCode()).isEqualTo(Objects.hash(null, null, null, null, null));
    }

    @Test
    @DisplayName("HashCode should work with mixed null and non-null values")
    void hashCodeShouldWorkWithMixedNullAndNonNullValues() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("path", "/test/path");
        // fileName, mimeType, metadata, and otherAttributes will be null

        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        int expectedHashCode = Objects.hash("/test/path", null, null, null, null);

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }

    @Test
    @DisplayName("HashCode should work with all non-null values")
    void hashCodeShouldWorkWithAllNonNullValues() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("path", "/test/path");
        attributes.put("fileName", "test.txt");
        attributes.put("mimeType", "text/plain");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");
        attributes.put("metadata", metadata);
        attributes.put("extra", "value");

        StorageResponseAttributes response = new StorageResponseAttributes(attributes);

        int expectedHashCode = Objects.hash("/test/path", "test.txt", "text/plain", metadata, response.getOtherAttributes());

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }

    // Helper methods to create metadata maps for testing
    private static Map<String, Object> createMetadataMap() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");
        metadata.put("number", 123);
        return metadata;
    }

    private static Map<String, Object> createDifferentMetadataMap() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "differentValue");
        metadata.put("number", 456);
        return metadata;
    }
} 
