package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

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
  // Happy path is not tested here as it requires MetadataFilterHelper.fromExpression, which is already covered in its own test.
}
