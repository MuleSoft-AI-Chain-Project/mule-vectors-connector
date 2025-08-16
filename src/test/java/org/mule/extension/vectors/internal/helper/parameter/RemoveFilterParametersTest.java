package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

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
}
