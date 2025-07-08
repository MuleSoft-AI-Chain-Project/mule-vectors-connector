package org.mule.extension.vectors.internal.helper.parameter;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.assertj.core.api.Assertions.*;

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
} 