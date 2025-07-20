package org.mule.extension.vectors.api.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import org.mule.extension.vectors.internal.helper.parameter.SearchFilterParameters;
import org.mule.extension.vectors.internal.helper.parameter.RemoveFilterParameters;

@DisplayName("StoreResponseAttributes Tests")
class StoreResponseAttributesTest {

    @Test
    @DisplayName("Should create StoreResponseAttributes with all fields")
    void shouldCreateStoreResponseAttributesWithAllFields() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("storeName", "my-store");
        attributes.put("searchFilter", createSearchFilterParameters("category = 'tech'"));
        attributes.put("extra", "value");

        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        assertThat(response.getStoreName()).isEqualTo("my-store");
        assertThat(response.getMetadataCondition()).isEqualTo("category = 'tech'");
        assertThat(response.getIds()).isNull();
        assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
    }

    @Test
    @DisplayName("Should create StoreResponseAttributes with remove filter")
    void shouldCreateStoreResponseAttributesWithRemoveFilter() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("storeName", "my-store");
        attributes.put("removeFilter", createRemoveFilterParameters("category = 'tech'", Arrays.asList("id1", "id2")));
        attributes.put("extra", "value");

        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        assertThat(response.getStoreName()).isEqualTo("my-store");
        assertThat(response.getMetadataCondition()).isEqualTo("category = 'tech'");
        assertThat(response.getIds()).containsExactly("id1", "id2");
        assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
    }

    @Test
    @DisplayName("Should create StoreResponseAttributes with null values")
    void shouldCreateStoreResponseAttributesWithNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("extra", "value");

        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        assertThat(response.getStoreName()).isNull();
        assertThat(response.getMetadataCondition()).isNull();
        assertThat(response.getIds()).isNull();
        assertThat(response.getOtherAttributes()).containsEntry("extra", "value");
    }

    @Test
    @DisplayName("Should create StoreResponseAttributes with empty map")
    void shouldCreateStoreResponseAttributesWithEmptyMap() {
        HashMap<String, Object> attributes = new HashMap<>();

        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        assertThat(response.getStoreName()).isNull();
        assertThat(response.getMetadataCondition()).isNull();
        assertThat(response.getIds()).isNull();
        assertThat(response.getOtherAttributes()).isEmpty();
    }

    @Test
    @DisplayName("Equals should return true for same object")
    void equalsShouldReturnTrueForSameObject() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("storeName", "my-store");
        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        assertThat(response.equals(response)).isTrue();
    }

    @Test
    @DisplayName("Equals should return false for null")
    void equalsShouldReturnFalseForNull() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("storeName", "my-store");
        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        assertThat(response.equals(null)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false for different class")
    void equalsShouldReturnFalseForDifferentClass() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("storeName", "my-store");
        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        assertThat(response.equals("string")).isFalse();
    }

    @Test
    @DisplayName("Equals should return true for identical objects")
    void equalsShouldReturnTrueForIdenticalObjects() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");
        attributes1.put("searchFilter", createSearchFilterParameters("category = 'tech'"));
        attributes1.put("extra", "value");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("searchFilter", createSearchFilterParameters("category = 'tech'"));
        attributes2.put("extra", "value");

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isTrue();
        assertThat(response2.equals(response1)).isTrue();
    }

    @Test
    @DisplayName("Equals should return false when storeName differs")
    void equalsShouldReturnFalseWhenStoreNameDiffers() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");
        attributes1.put("searchFilter", createSearchFilterParameters("category = 'tech'"));

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "other-store");
        attributes2.put("searchFilter", createSearchFilterParameters("category = 'tech'"));

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false when metadataCondition differs")
    void equalsShouldReturnFalseWhenMetadataConditionDiffers() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");
        attributes1.put("searchFilter", createSearchFilterParameters("category = 'tech'"));

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("searchFilter", createSearchFilterParameters("category = 'news'"));

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false when ids differs")
    void equalsShouldReturnFalseWhenIdsDiffers() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");
        attributes1.put("removeFilter", createRemoveFilterParameters("category = 'tech'", Arrays.asList("id1", "id2")));

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("removeFilter", createRemoveFilterParameters("category = 'tech'", Arrays.asList("id1", "id3")));

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should return false when otherAttributes differs")
    void equalsShouldReturnFalseWhenOtherAttributesDiffers() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");
        attributes1.put("extra", "value1");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("extra", "value2");

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle null storeName correctly")
    void equalsShouldHandleNullStoreNameCorrectly() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("searchFilter", createSearchFilterParameters("category = 'tech'"));

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("searchFilter", createSearchFilterParameters("category = 'tech'"));

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle null metadataCondition correctly")
    void equalsShouldHandleNullMetadataConditionCorrectly() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("searchFilter", createSearchFilterParameters("category = 'tech'"));

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle null ids correctly")
    void equalsShouldHandleNullIdsCorrectly() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("removeFilter", createRemoveFilterParameters("category = 'tech'", Arrays.asList("id1", "id2")));

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle null otherAttributes correctly")
    void equalsShouldHandleNullOtherAttributesCorrectly() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("extra", "value");

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("HashCode should be consistent")
    void hashCodeShouldBeConsistent() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("storeName", "my-store");
        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        int hashCode1 = response.hashCode();
        int hashCode2 = response.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    @DisplayName("HashCode should be equal for equal objects")
    void hashCodeShouldBeEqualForEqualObjects() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");
        attributes1.put("searchFilter", createSearchFilterParameters("category = 'tech'"));

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("searchFilter", createSearchFilterParameters("category = 'tech'"));

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("HashCode should be different for different objects")
    void hashCodeShouldBeDifferentForDifferentObjects() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "other-store");

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.hashCode()).isNotEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("HashCode should work with null values")
    void hashCodeShouldWorkWithNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        // Expected: result = 0; result = 31 * result + 0; result = 31 * result + 0; result = 31 * result + 0
        int expectedHashCode = 0;
        expectedHashCode = 31 * expectedHashCode + 0; // ids null
        expectedHashCode = 31 * expectedHashCode + 0; // metadataCondition null
        expectedHashCode = 31 * expectedHashCode + 0; // otherAttributes null

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }

    @Test
    @DisplayName("HashCode should work with all null values")
    void hashCodeShouldWorkWithAllNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        assertThat(response.hashCode()).isEqualTo(0);
    }

    @Test
    @DisplayName("Equals should handle case where this.storeName is null but that.storeName is not null")
    void equalsShouldHandleThisStoreNameNullButThatStoreNameNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        // No storeName in attributes1

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where this.ids is null but that.ids is not null")
    void equalsShouldHandleThisIdsNullButThatIdsNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("removeFilter", createRemoveFilterParameters("category = 'tech'", Arrays.asList("id1", "id2")));

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where this.metadataCondition is null but that.metadataCondition is not null")
    void equalsShouldHandleThisMetadataConditionNullButThatMetadataConditionNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("searchFilter", createSearchFilterParameters("category = 'tech'"));

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where this.otherAttributes is null but that.otherAttributes is not null")
    void equalsShouldHandleThisOtherAttributesNullButThatOtherAttributesNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");
        attributes2.put("extra", "value");

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where that.storeName is null but this.storeName is not null")
    void equalsShouldHandleThatStoreNameNullButThisStoreNameNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");

        HashMap<String, Object> attributes2 = new HashMap<>();
        // No storeName in attributes2

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where that.ids is null but this.ids is not null")
    void equalsShouldHandleThatIdsNullButThisIdsNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");
        attributes1.put("removeFilter", createRemoveFilterParameters("category = 'tech'", Arrays.asList("id1", "id2")));

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where that.metadataCondition is null but this.metadataCondition is not null")
    void equalsShouldHandleThatMetadataConditionNullButThisMetadataConditionNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");
        attributes1.put("searchFilter", createSearchFilterParameters("category = 'tech'"));

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("Equals should handle case where that.otherAttributes is null but this.otherAttributes is not null")
    void equalsShouldHandleThatOtherAttributesNullButThisOtherAttributesNotNull() {
        HashMap<String, Object> attributes1 = new HashMap<>();
        attributes1.put("storeName", "my-store");
        attributes1.put("extra", "value");

        HashMap<String, Object> attributes2 = new HashMap<>();
        attributes2.put("storeName", "my-store");

        StoreResponseAttributes response1 = new StoreResponseAttributes(attributes1);
        StoreResponseAttributes response2 = new StoreResponseAttributes(attributes2);

        assertThat(response1.equals(response2)).isFalse();
        assertThat(response2.equals(response1)).isFalse();
    }

    @Test
    @DisplayName("HashCode should work with mixed null and non-null values")
    void hashCodeShouldWorkWithMixedNullAndNonNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("storeName", "my-store");
        // ids, metadataCondition, and otherAttributes will be null

        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        int expectedHashCode = "my-store".hashCode();
        expectedHashCode = 31 * expectedHashCode + 0; // ids null
        expectedHashCode = 31 * expectedHashCode + 0; // metadataCondition null
        expectedHashCode = 31 * expectedHashCode + 0; // otherAttributes null

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }

    @Test
    @DisplayName("HashCode should work with all non-null values")
    void hashCodeShouldWorkWithAllNonNullValues() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("storeName", "my-store");
        attributes.put("searchFilter", createSearchFilterParameters("category = 'tech'"));
        attributes.put("removeFilter", createRemoveFilterParameters("category = 'tech'", Arrays.asList("id1", "id2")));
        attributes.put("extra", "value");

        StoreResponseAttributes response = new StoreResponseAttributes(attributes);

        int expectedHashCode = "my-store".hashCode();
        expectedHashCode = 31 * expectedHashCode + Arrays.asList("id1", "id2").hashCode(); // ids
        expectedHashCode = 31 * expectedHashCode + "category = 'tech'".hashCode(); // metadataCondition
        expectedHashCode = 31 * expectedHashCode + response.getOtherAttributes().hashCode(); // otherAttributes

        assertThat(response.hashCode()).isEqualTo(expectedHashCode);
    }

    // Helper methods to create filter parameters for testing
    private SearchFilterParameters createSearchFilterParameters(String condition) {
        SearchFilterParameters filter = new SearchFilterParameters();
        try {
            java.lang.reflect.Field conditionField = SearchFilterParameters.class.getDeclaredField("condition");
            conditionField.setAccessible(true);
            conditionField.set(filter, condition);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set condition field", e);
        }
        return filter;
    }

    private RemoveFilterParameters createRemoveFilterParameters(String condition, List<String> ids) {
        RemoveFilterParameters filter = new RemoveFilterParameters();
        try {
            java.lang.reflect.Field conditionField = RemoveFilterParameters.class.getDeclaredField("condition");
            conditionField.setAccessible(true);
            conditionField.set(filter, condition);
            
            java.lang.reflect.Field idsField = RemoveFilterParameters.class.getDeclaredField("ids");
            idsField.setAccessible(true);
            idsField.set(filter, ids);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set filter fields", e);
        }
        return filter;
    }
} 
