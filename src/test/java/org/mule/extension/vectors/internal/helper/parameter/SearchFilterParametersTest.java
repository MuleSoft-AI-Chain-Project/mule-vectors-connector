package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.internal.helper.parameter.SearchFilterParameters;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

class SearchFilterParametersTest {

  @Test
  void getCondition_shouldReflectSetValue() throws Exception {
    SearchFilterParameters params = new SearchFilterParameters();
    Field condField = params.getClass().getDeclaredField("condition");
    condField.setAccessible(true);
    condField.set(params, "foo = 'bar'");
    assertThat(params.getCondition()).isEqualTo("foo = 'bar'");
  }

  @Test
  void isConditionSet_shouldReturnTrueIfConditionPresent() throws Exception {
    SearchFilterParameters params = new SearchFilterParameters();
    Field condField = params.getClass().getDeclaredField("condition");
    condField.setAccessible(true);
    condField.set(params, "foo = 'bar'");
    assertThat(params.isConditionSet()).isTrue();
  }

  @Test
  void isConditionSet_shouldReturnFalseIfConditionNull() {
    SearchFilterParameters params = new SearchFilterParameters();
    assertThat(params.isConditionSet()).isFalse();
  }

  @Test
  void equals_sameObject_shouldReturnTrue() throws Exception {
    SearchFilterParameters params = createParams("foo = 'bar'");
    assertThat(params).isEqualTo(params);
  }

  @Test
  void equals_sameCondition_shouldReturnTrue() throws Exception {
    SearchFilterParameters p1 = createParams("foo = 'bar'");
    SearchFilterParameters p2 = createParams("foo = 'bar'");
    assertThat(p1).isEqualTo(p2);
  }

  @Test
  void equals_differentCondition_shouldReturnFalse() throws Exception {
    SearchFilterParameters p1 = createParams("foo = 'bar'");
    SearchFilterParameters p2 = createParams("baz = 'qux'");
    assertThat(p1).isNotEqualTo(p2);
  }

  @Test
  void equals_null_shouldReturnFalse() throws Exception {
    SearchFilterParameters params = createParams("foo = 'bar'");
    assertThat(params).isNotEqualTo(null);
  }

  @Test
  void equals_differentClass_shouldReturnFalse() throws Exception {
    SearchFilterParameters params = createParams("foo = 'bar'");
    assertThat(params).isNotEqualTo("foo = 'bar'");
  }

  @Test
  void hashCode_sameCondition_shouldBeEqual() throws Exception {
    SearchFilterParameters p1 = createParams("foo = 'bar'");
    SearchFilterParameters p2 = createParams("foo = 'bar'");
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  void hashCode_differentCondition_shouldDiffer() throws Exception {
    SearchFilterParameters p1 = createParams("foo = 'bar'");
    SearchFilterParameters p2 = createParams("baz = 'qux'");
    assertThat(p1.hashCode()).isNotEqualTo(p2.hashCode());
  }

  private SearchFilterParameters createParams(String condition) throws Exception {
    SearchFilterParameters params = new SearchFilterParameters();
    Field condField = params.getClass().getDeclaredField("condition");
    condField.setAccessible(true);
    condField.set(params, condition);
    return params;
  }
}
