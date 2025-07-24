package org.mule.extension.vectors.api.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link ParserResponseAttributes}.
 */
@DisplayName("ParserResponseAttributes Tests")
class ParserResponseAttributesTest {

  @Test
  @DisplayName("Should create ParserResponseAttributes with all fields")
  void shouldCreateParserResponseAttributesWithAllFields() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("documentParserName", "pdf-parser");
    attributes.put("timestamp", 1234567890L);
    attributes.put("fileSize", 1024);

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    assertThat(response.getDocumentParserName()).isEqualTo("pdf-parser");
    assertThat(response.getOtherAttributes()).containsEntry("timestamp", 1234567890L);
    assertThat(response.getOtherAttributes()).containsEntry("fileSize", 1024);
    assertThat(response.getOtherAttributes()).doesNotContainKey("documentParserName");
  }

  @Test
  @DisplayName("Should create ParserResponseAttributes with null documentParserName")
  void shouldCreateParserResponseAttributesWithNullDocumentParserName() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("documentParserName", null);
    attributes.put("timestamp", 1234567890L);

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    assertThat(response.getDocumentParserName()).isNull();
    assertThat(response.getOtherAttributes()).containsEntry("timestamp", 1234567890L);
    assertThat(response.getOtherAttributes()).doesNotContainKey("documentParserName");
  }

  @Test
  @DisplayName("Should create ParserResponseAttributes with empty map")
  void shouldCreateParserResponseAttributesWithEmptyMap() {
    HashMap<String, Object> attributes = new HashMap<>();

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    assertThat(response.getDocumentParserName()).isNull();
    assertThat(response.getOtherAttributes()).isEmpty();
  }

  @Test
  @DisplayName("Should create ParserResponseAttributes with missing documentParserName")
  void shouldCreateParserResponseAttributesWithMissingDocumentParserName() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("timestamp", 1234567890L);
    attributes.put("fileSize", 1024);

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    assertThat(response.getDocumentParserName()).isNull();
    assertThat(response.getOtherAttributes()).containsEntry("timestamp", 1234567890L);
    assertThat(response.getOtherAttributes()).containsEntry("fileSize", 1024);
  }

  @Test
  @DisplayName("Should create ParserResponseAttributes with only documentParserName")
  void shouldCreateParserResponseAttributesWithOnlyDocumentParserName() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("documentParserName", "text-parser");

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    assertThat(response.getDocumentParserName()).isEqualTo("text-parser");
    assertThat(response.getOtherAttributes()).isEmpty();
  }

  @Test
  @DisplayName("Should handle mixed type values in otherAttributes")
  void shouldHandleMixedTypeValuesInOtherAttributes() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("documentParserName", "mixed-parser");
    attributes.put("stringValue", "test");
    attributes.put("intValue", 42);
    attributes.put("doubleValue", 3.14);
    attributes.put("booleanValue", true);
    attributes.put("nullValue", null);

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    assertThat(response.getDocumentParserName()).isEqualTo("mixed-parser");
    assertThat(response.getOtherAttributes()).containsEntry("stringValue", "test");
    assertThat(response.getOtherAttributes()).containsEntry("intValue", 42);
    assertThat(response.getOtherAttributes()).containsEntry("doubleValue", 3.14);
    assertThat(response.getOtherAttributes()).containsEntry("booleanValue", true);
    assertThat(response.getOtherAttributes()).containsEntry("nullValue", null);
  }

  @Test
  @DisplayName("Should return defensive copy of otherAttributes")
  void shouldReturnDefensiveCopyOfOtherAttributes() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("documentParserName", "defensive-parser");
    attributes.put("originalValue", "original");

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    Map<String, Object> otherAttrs = response.getOtherAttributes();
    otherAttrs.put("modifiedValue", "modified");

    // Original response should not be affected
    assertThat(response.getOtherAttributes()).doesNotContainKey("modifiedValue");
    assertThat(response.getOtherAttributes()).containsEntry("originalValue", "original");
  }

  @Test
  @DisplayName("Should have correct equals behavior")
  void shouldHaveCorrectEqualsBehavior() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("documentParserName", "parser1");
    attributes1.put("timestamp", 1234567890L);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("documentParserName", "parser1");
    attributes2.put("timestamp", 1234567890L);

    ParserResponseAttributes response1 = new ParserResponseAttributes(attributes1);
    ParserResponseAttributes response2 = new ParserResponseAttributes(attributes2);

    assertThat(response1).isEqualTo(response2);
    assertThat(response1).isEqualTo(response1);
    assertThat(response1).isNotEqualTo(null);
    assertThat(response1).isNotEqualTo("string");
  }

  @Test
  @DisplayName("Should have correct hashCode behavior")
  void shouldHaveCorrectHashCodeBehavior() {
    HashMap<String, Object> attributes1 = new HashMap<>();
    attributes1.put("documentParserName", "parser1");
    attributes1.put("timestamp", 1234567890L);

    HashMap<String, Object> attributes2 = new HashMap<>();
    attributes2.put("documentParserName", "parser1");
    attributes2.put("timestamp", 1234567890L);

    ParserResponseAttributes response1 = new ParserResponseAttributes(attributes1);
    ParserResponseAttributes response2 = new ParserResponseAttributes(attributes2);

    assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
  }

  @Test
  @DisplayName("Should have correct toString behavior")
  void shouldHaveCorrectToStringBehavior() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("documentParserName", "test-parser");
    attributes.put("timestamp", 1234567890L);

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    String toString = response.toString();
    assertThat(toString).contains("ParserResponseAttributes");
    assertThat(toString).contains("documentParserName='test-parser'");
    assertThat(toString).contains("otherAttributes=");
  }

  @Test
  @DisplayName("Should handle empty string documentParserName")
  void shouldHandleEmptyStringDocumentParserName() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("documentParserName", "");
    attributes.put("timestamp", 1234567890L);

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    assertThat(response.getDocumentParserName()).isEqualTo("");
    assertThat(response.getOtherAttributes()).containsEntry("timestamp", 1234567890L);
  }

  @Test
  @DisplayName("Should handle whitespace string documentParserName")
  void shouldHandleWhitespaceStringDocumentParserName() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("documentParserName", "   ");
    attributes.put("timestamp", 1234567890L);

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    assertThat(response.getDocumentParserName()).isEqualTo("   ");
    assertThat(response.getOtherAttributes()).containsEntry("timestamp", 1234567890L);
  }

  @Test
  @DisplayName("Should handle special characters in documentParserName")
  void shouldHandleSpecialCharactersInDocumentParserName() {
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("documentParserName", "parser-with-special-chars!@#$%^&*()");
    attributes.put("timestamp", 1234567890L);

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    assertThat(response.getDocumentParserName()).isEqualTo("parser-with-special-chars!@#$%^&*()");
    assertThat(response.getOtherAttributes()).containsEntry("timestamp", 1234567890L);
  }

  @Test
  @DisplayName("Should handle very long documentParserName")
  void shouldHandleVeryLongDocumentParserName() {
    String longName = "a".repeat(1000);
    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put("documentParserName", longName);
    attributes.put("timestamp", 1234567890L);

    ParserResponseAttributes response = new ParserResponseAttributes(attributes);

    assertThat(response.getDocumentParserName()).isEqualTo(longName);
    assertThat(response.getDocumentParserName()).hasSize(1000);
  }
}
