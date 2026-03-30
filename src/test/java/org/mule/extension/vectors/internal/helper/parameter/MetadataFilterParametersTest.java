package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.api.helper.parameter.MetadataFilterParameters;

import org.junit.jupiter.api.Test;

class MetadataFilterParametersTest {

  static class TestMetadataFilterParameters extends MetadataFilterParameters {

    private String cond;

    TestMetadataFilterParameters(String cond) {
      this.cond = cond;
    }

    @Override
    public String getCondition() {
      return cond;
    }
  }

  @Test
  void isConditionSet_shouldReturnTrueIfConditionPresent() {
    var params = new TestMetadataFilterParameters("foo = 'bar'");
    assertThat(params.isConditionSet()).isTrue();
  }

  @Test
  void isConditionSet_shouldReturnFalseIfConditionNull() {
    var params = new TestMetadataFilterParameters(null);
    assertThat(params.isConditionSet()).isFalse();
  }

  @Test
  void buildMetadataFilter_shouldThrowIfConditionNotSet() {
    var params = new TestMetadataFilterParameters(null);
    assertThatThrownBy(params::buildMetadataFilter)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Filter condition is not set");
  }

  @Test
  void equals_sameObject_shouldReturnTrue() {
    var params = new TestMetadataFilterParameters("foo = 'bar'");
    assertThat(params).isEqualTo(params);
  }

  @Test
  void equals_sameCondition_shouldReturnTrue() {
    var params1 = new TestMetadataFilterParameters("foo = 'bar'");
    var params2 = new TestMetadataFilterParameters("foo = 'bar'");
    assertThat(params1).isEqualTo(params2);
  }

  @Test
  void equals_differentCondition_shouldReturnFalse() {
    var params1 = new TestMetadataFilterParameters("foo = 'bar'");
    var params2 = new TestMetadataFilterParameters("baz = 'qux'");
    assertThat(params1).isNotEqualTo(params2);
  }

  @Test
  void equals_null_shouldReturnFalse() {
    var params = new TestMetadataFilterParameters("foo = 'bar'");
    assertThat(params).isNotEqualTo(null);
  }

  @Test
  void equals_differentClass_shouldReturnFalse() {
    var params = new TestMetadataFilterParameters("foo = 'bar'");
    assertThat(params).isNotEqualTo("foo = 'bar'");
  }

  @Test
  void equals_bothNullCondition_shouldReturnTrue() {
    var params1 = new TestMetadataFilterParameters(null);
    var params2 = new TestMetadataFilterParameters(null);
    assertThat(params1).isEqualTo(params2);
  }

  @Test
  void hashCode_sameCondition_shouldBeEqual() {
    var params1 = new TestMetadataFilterParameters("foo = 'bar'");
    var params2 = new TestMetadataFilterParameters("foo = 'bar'");
    assertThat(params1.hashCode()).isEqualTo(params2.hashCode());
  }

  @Test
  void hashCode_differentCondition_shouldDiffer() {
    var params1 = new TestMetadataFilterParameters("foo = 'bar'");
    var params2 = new TestMetadataFilterParameters("baz = 'qux'");
    assertThat(params1.hashCode()).isNotEqualTo(params2.hashCode());
  }
}
