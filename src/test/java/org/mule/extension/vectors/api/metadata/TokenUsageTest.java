package org.mule.extension.vectors.api.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("TokenUsage Tests")
class TokenUsageTest {

  @Test
  @DisplayName("Should create TokenUsage with valid parameters")
  void shouldCreateTokenUsageWithValidParameters() {
    TokenUsage tokenUsage = new TokenUsage(10, 20, 30);

    assertThat(tokenUsage.getInputCount()).isEqualTo(10);
    assertThat(tokenUsage.getOutputCount()).isEqualTo(20);
    assertThat(tokenUsage.getTotalCount()).isEqualTo(30);
  }

  @Test
  @DisplayName("Should create TokenUsage with zero values")
  void shouldCreateTokenUsageWithZeroValues() {
    TokenUsage tokenUsage = new TokenUsage(0, 0, 0);

    assertThat(tokenUsage.getInputCount()).isEqualTo(0);
    assertThat(tokenUsage.getOutputCount()).isEqualTo(0);
    assertThat(tokenUsage.getTotalCount()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should create TokenUsage with negative values")
  void shouldCreateTokenUsageWithNegativeValues() {
    TokenUsage tokenUsage = new TokenUsage(-5, -10, -15);

    assertThat(tokenUsage.getInputCount()).isEqualTo(-5);
    assertThat(tokenUsage.getOutputCount()).isEqualTo(-10);
    assertThat(tokenUsage.getTotalCount()).isEqualTo(-15);
  }

  @Test
  @DisplayName("Equals should return true for same object")
  void equalsShouldReturnTrueForSameObject() {
    TokenUsage tokenUsage = new TokenUsage(10, 20, 30);

    assertThat(tokenUsage.equals(tokenUsage)).isTrue();
  }

  @Test
  @DisplayName("Equals should return false for null")
  void equalsShouldReturnFalseForNull() {
    TokenUsage tokenUsage = new TokenUsage(10, 20, 30);

    assertThat(tokenUsage.equals(null)).isFalse();
  }

  @Test
  @DisplayName("Equals should return false for different class")
  void equalsShouldReturnFalseForDifferentClass() {
    TokenUsage tokenUsage = new TokenUsage(10, 20, 30);
    Object otherObject = "string";

    assertThat(tokenUsage.equals(otherObject)).isFalse();
  }

  @Test
  @DisplayName("Equals should return true for identical objects")
  void equalsShouldReturnTrueForIdenticalObjects() {
    TokenUsage tokenUsage1 = new TokenUsage(10, 20, 30);
    TokenUsage tokenUsage2 = new TokenUsage(10, 20, 30);

    assertThat(tokenUsage1.equals(tokenUsage2)).isTrue();
    assertThat(tokenUsage2.equals(tokenUsage1)).isTrue();
  }

  @ParameterizedTest
  @MethodSource("provideDifferentFieldValues")
  @DisplayName("Equals should return false when specific fields differ")
  void equalsShouldReturnFalseWhenFieldsDiffer(String testCase, TokenUsage tokenUsage1, TokenUsage tokenUsage2) {
    assertThat(tokenUsage1.equals(tokenUsage2)).isFalse();
    assertThat(tokenUsage2.equals(tokenUsage1)).isFalse();
  }

  private static Stream<Arguments> provideDifferentFieldValues() {
    return Stream.of(
                     Arguments.of("inputCount differs",
                                  new TokenUsage(10, 20, 30),
                                  new TokenUsage(15, 20, 30)),
                     Arguments.of("outputCount differs",
                                  new TokenUsage(10, 20, 30),
                                  new TokenUsage(10, 25, 30)),
                     Arguments.of("totalCount differs",
                                  new TokenUsage(10, 20, 30),
                                  new TokenUsage(10, 20, 35)));
  }

  @Test
  @DisplayName("Equals should return true for objects with zero values")
  void equalsShouldReturnTrueForObjectsWithZeroValues() {
    TokenUsage tokenUsage1 = new TokenUsage(0, 0, 0);
    TokenUsage tokenUsage2 = new TokenUsage(0, 0, 0);

    assertThat(tokenUsage1.equals(tokenUsage2)).isTrue();
    assertThat(tokenUsage2.equals(tokenUsage1)).isTrue();
  }

  @Test
  @DisplayName("Equals should return true for objects with negative values")
  void equalsShouldReturnTrueForObjectsWithNegativeValues() {
    TokenUsage tokenUsage1 = new TokenUsage(-5, -10, -15);
    TokenUsage tokenUsage2 = new TokenUsage(-5, -10, -15);

    assertThat(tokenUsage1.equals(tokenUsage2)).isTrue();
    assertThat(tokenUsage2.equals(tokenUsage1)).isTrue();
  }

  @Test
  @DisplayName("HashCode should be consistent")
  void hashCodeShouldBeConsistent() {
    TokenUsage tokenUsage = new TokenUsage(10, 20, 30);
    int hashCode1 = tokenUsage.hashCode();
    int hashCode2 = tokenUsage.hashCode();

    assertThat(hashCode1).isEqualTo(hashCode2);
  }

  @Test
  @DisplayName("HashCode should be equal for equal objects")
  void hashCodeShouldBeEqualForEqualObjects() {
    TokenUsage tokenUsage1 = new TokenUsage(10, 20, 30);
    TokenUsage tokenUsage2 = new TokenUsage(10, 20, 30);

    assertThat(tokenUsage1.hashCode()).isEqualTo(tokenUsage2.hashCode());
  }

  @Test
  @DisplayName("HashCode should be different for different objects")
  void hashCodeShouldBeDifferentForDifferentObjects() {
    TokenUsage tokenUsage1 = new TokenUsage(10, 20, 30);
    TokenUsage tokenUsage2 = new TokenUsage(15, 20, 30);

    assertThat(tokenUsage1.hashCode()).isNotEqualTo(tokenUsage2.hashCode());
  }

  @Test
  @DisplayName("HashCode should work with zero values")
  void hashCodeShouldWorkWithZeroValues() {
    TokenUsage tokenUsage = new TokenUsage(0, 0, 0);

    assertThat(tokenUsage.hashCode()).isEqualTo(0);
  }

  @Test
  @DisplayName("HashCode should work with negative values")
  void hashCodeShouldWorkWithNegativeValues() {
    TokenUsage tokenUsage = new TokenUsage(-5, -10, -15);

    // The expected hashCode calculation: result = -5; result = 31 * result + (-10); result = 31 * result + (-15)
    int expectedHashCode = -5;
    expectedHashCode = 31 * expectedHashCode + (-10);
    expectedHashCode = 31 * expectedHashCode + (-15);

    assertThat(tokenUsage.hashCode()).isEqualTo(expectedHashCode);
  }

  @Test
  @DisplayName("HashCode should work with large values")
  void hashCodeShouldWorkWithLargeValues() {
    TokenUsage tokenUsage = new TokenUsage(1000, 2000, 3000);

    int expectedHashCode = 1000;
    expectedHashCode = 31 * expectedHashCode + 2000;
    expectedHashCode = 31 * expectedHashCode + 3000;

    assertThat(tokenUsage.hashCode()).isEqualTo(expectedHashCode);
  }
}
