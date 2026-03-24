package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.api.helper.parameter.RemoveFilterParameters;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;

class RemoveFilterParametersTest {

  @Test
  void getIds_shouldReflectSetValue() throws Exception {
    RemoveFilterParameters params = new RemoveFilterParameters();
    Field idsField = params.getClass().getDeclaredField("ids");
    idsField.setAccessible(true);
    idsField.set(params, List.of("id1", "id2"));
    assertThat(params.getIds()).containsExactly("id1", "id2");
  }

  @Test
  void getCondition_shouldReflectSetValue() throws Exception {
    RemoveFilterParameters params = new RemoveFilterParameters();
    Field condField = params.getClass().getDeclaredField("condition");
    condField.setAccessible(true);
    condField.set(params, "foo = 'bar'");
    assertThat(params.getCondition()).isEqualTo("foo = 'bar'");
  }

  @Test
  void validate_happyPath_noException() throws Exception {
    RemoveFilterParameters params = new RemoveFilterParameters();
    Field idsField = params.getClass().getDeclaredField("ids");
    idsField.setAccessible(true);
    idsField.set(params, List.of("id1"));
    Field condField = params.getClass().getDeclaredField("condition");
    condField.setAccessible(true);
    condField.set(params, "");
    assertThatCode(params::validate).doesNotThrowAnyException();
  }

  @Test
  void validate_errorBranch_bothSet_throws() throws Exception {
    RemoveFilterParameters params = new RemoveFilterParameters();
    Field idsField = params.getClass().getDeclaredField("ids");
    idsField.setAccessible(true);
    idsField.set(params, List.of("id1"));
    Field condField = params.getClass().getDeclaredField("condition");
    condField.setAccessible(true);
    condField.set(params, "foo = 'bar'");
    assertThatThrownBy(params::validate)
        .isInstanceOf(ModuleException.class)
        .hasMessageContaining("mutually exclusive");
  }

  @Test
  void isIdsSet_shouldReturnTrueIfIdsPresent() throws Exception {
    RemoveFilterParameters params = new RemoveFilterParameters();
    Field idsField = params.getClass().getDeclaredField("ids");
    idsField.setAccessible(true);
    idsField.set(params, List.of("id1"));
    assertThat(params.isIdsSet()).isTrue();
  }

  @Test
  void isIdsSet_shouldReturnFalseIfIdsNullOrEmpty() throws Exception {
    RemoveFilterParameters params = new RemoveFilterParameters();
    assertThat(params.isIdsSet()).isFalse();
    Field idsField = params.getClass().getDeclaredField("ids");
    idsField.setAccessible(true);
    idsField.set(params, List.of());
    assertThat(params.isIdsSet()).isFalse();
  }

  @Test
  void equals_sameObject_shouldReturnTrue() throws Exception {
    RemoveFilterParameters params = createParams(List.of("id1"), "cond");
    assertThat(params).isEqualTo(params);
  }

  @Test
  void equals_sameValues_shouldReturnTrue() throws Exception {
    RemoveFilterParameters p1 = createParams(List.of("id1"), "cond");
    RemoveFilterParameters p2 = createParams(List.of("id1"), "cond");
    assertThat(p1).isEqualTo(p2);
  }

  @Test
  void equals_differentIds_shouldReturnFalse() throws Exception {
    RemoveFilterParameters p1 = createParams(List.of("id1"), "cond");
    RemoveFilterParameters p2 = createParams(List.of("id2"), "cond");
    assertThat(p1).isNotEqualTo(p2);
  }

  @Test
  void equals_differentCondition_shouldReturnFalse() throws Exception {
    RemoveFilterParameters p1 = createParams(List.of("id1"), "cond1");
    RemoveFilterParameters p2 = createParams(List.of("id1"), "cond2");
    assertThat(p1).isNotEqualTo(p2);
  }

  @Test
  void equals_null_shouldReturnFalse() throws Exception {
    RemoveFilterParameters params = createParams(List.of("id1"), "cond");
    assertThat(params).isNotEqualTo(null);
  }

  @Test
  void equals_differentClass_shouldReturnFalse() throws Exception {
    RemoveFilterParameters params = createParams(List.of("id1"), "cond");
    assertThat(params).isNotEqualTo("string");
  }

  @Test
  void hashCode_sameValues_shouldBeEqual() throws Exception {
    RemoveFilterParameters p1 = createParams(List.of("id1"), "cond");
    RemoveFilterParameters p2 = createParams(List.of("id1"), "cond");
    assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
  }

  @Test
  void hashCode_differentValues_shouldDiffer() throws Exception {
    RemoveFilterParameters p1 = createParams(List.of("id1"), "cond1");
    RemoveFilterParameters p2 = createParams(List.of("id2"), "cond2");
    assertThat(p1.hashCode()).isNotEqualTo(p2.hashCode());
  }

  private RemoveFilterParameters createParams(List<String> ids, String condition) throws Exception {
    RemoveFilterParameters params = new RemoveFilterParameters();
    Field idsField = params.getClass().getDeclaredField("ids");
    idsField.setAccessible(true);
    idsField.set(params, ids);
    Field condField = params.getClass().getDeclaredField("condition");
    condField.setAccessible(true);
    condField.set(params, condition);
    return params;
  }
}
