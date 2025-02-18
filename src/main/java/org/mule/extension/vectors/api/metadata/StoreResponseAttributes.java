package org.mule.extension.vectors.api.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.mule.extension.vectors.internal.helper.parameter.MetadataFilterParameters;
import org.mule.runtime.extension.api.annotation.param.MediaType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

/**
 * Represents the attributes of a store operation response.
 * <p>
 * This class contains metadata about a store, including the store name, filter attributes,
 * and additional attributes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StoreResponseAttributes implements Serializable {

  /**
   * The name of the store.
   */
  private final String storeName;

  /**
   * The filter attribute associated with the store response.
   */
  private String filterCondition;

  /**
   * Additional attributes not explicitly defined as fields in this class.
   */
  private final HashMap<String, Object> otherAttributes;

  /**
   * Constructs a {@code StoreResponseAttributes} instance.
   *
   * @param requestAttributes a map containing attributes of the store operation response.
   *                          Expected keys include "storeName" and "searchFilter" or "removeFilter",
   *                          which are extracted and stored in their respective fields.
   *                          Remaining entries are stored in {@code otherAttributes}.
   */
  public StoreResponseAttributes(HashMap<String, Object> requestAttributes) {
    this.storeName = requestAttributes.containsKey("storeName") ? (String) requestAttributes.remove("storeName") : null;

    MetadataFilterParameters filterParams = requestAttributes.containsKey("searchFilter")
        ? (MetadataFilterParameters.SearchFilterParameters) requestAttributes.remove("searchFilter")
        : requestAttributes.containsKey("removeFilter")
            ? (MetadataFilterParameters.RemoveFilterParameters) requestAttributes.remove("removeFilter")
            : null;

    if (filterParams != null) {
      this.filterCondition = filterParams.getCondition();
    }

    this.otherAttributes = requestAttributes;
  }

  /**
   * Gets the name of the store.
   *
   * @return the store name, or {@code null} if not available.
   */
  public String getStoreName() {
    return storeName;
  }

  /**
   * Retrieves the filter condition.
   *
   * @return the current filter condition.
   */
  public String getFilterCondition() {
    return filterCondition;
  }

  /**
   * Gets additional attributes of the store response.
   * <p>
   * These are attributes not explicitly defined in this class.
   *
   * @return a map of additional attributes.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  public Map<String, Object> getOtherAttributes() {
    return otherAttributes;
  }
}
