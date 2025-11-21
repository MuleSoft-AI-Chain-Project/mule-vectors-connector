package org.mule.extension.vectors.api.metadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.extension.vectors.internal.helper.parameter.RemoveFilterParameters;
import org.mule.extension.vectors.internal.helper.parameter.SearchFilterParameters;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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
   * The list of IDs associated with the store response.
   */
  private List<String> ids;

  /**
   * The metadata condition associated with the store response.
   */
  private String metadataCondition;

  /**
   * Additional attributes not explicitly defined as fields in this class.
   */
  private transient final HashMap<String, Object> otherAttributes;

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

    if (requestAttributes.containsKey("searchFilter")) {

      SearchFilterParameters filterParams = (SearchFilterParameters) requestAttributes.remove("searchFilter");
      if (filterParams.isConditionSet())
        this.metadataCondition = filterParams.getCondition();

    }
    if (requestAttributes.containsKey("removeFilter")) {

      RemoveFilterParameters filterParams = (RemoveFilterParameters) requestAttributes.remove("removeFilter");
      if (filterParams.isConditionSet())
        this.metadataCondition = filterParams.getCondition();
      if (filterParams.isIdsSet())
        this.ids = filterParams.getIds();
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
   * Retrieves the metadata condition.
   *
   * @return the current metadata condition.
   */
  public String getMetadataCondition() {
    return metadataCondition;
  }

  /**
   * Retrieves the list of IDs.
   *
   * @return the list of IDs, or {@code null} if not available.
   */
  public List<String> getIds() {
    return ids;
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    StoreResponseAttributes that = (StoreResponseAttributes) o;

    if (storeName != null ? !storeName.equals(that.storeName) : that.storeName != null)
      return false;
    if (ids != null ? !ids.equals(that.ids) : that.ids != null)
      return false;
    if (metadataCondition != null ? !metadataCondition.equals(that.metadataCondition) : that.metadataCondition != null)
      return false;
    return otherAttributes != null ? otherAttributes.equals(that.otherAttributes) : that.otherAttributes == null;
  }

  @Override
  public int hashCode() {
    int result = storeName != null ? storeName.hashCode() : 0;
    result = 31 * result + (ids != null ? ids.hashCode() : 0);
    result = 31 * result + (metadataCondition != null ? metadataCondition.hashCode() : 0);
    result = 31 * result + (otherAttributes != null ? otherAttributes.hashCode() : 0);
    return result;
  }
}
