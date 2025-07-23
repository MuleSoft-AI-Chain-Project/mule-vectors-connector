package org.mule.extension.vectors.api.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.mule.runtime.extension.api.annotation.param.MediaType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

/**
 * Represents the attributes of a parser operation response.
 * <p>
 * This class contains metadata about a parser operation, such as segment size,
 * overlap size, and any additional attributes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChunkResponseAttributes implements Serializable {

  /**
   * The maximum size of segments in characters.
   */
  private final Integer maxSegmentSizeInChars;

  /**
   * The maximum overlap size between segments in characters.
   */
  private final Integer maxOverlapSizeInChars;

  /**
   * Additional attributes not explicitly defined as fields in this class.
   */
  private transient final HashMap<String, Object> otherAttributes;

  /**
   * Constructs a {@code ParserResponseAttributes} instance.
   *
   * @param requestAttributes a map containing parser operation attributes.
   *                          Expected keys include "maxSegmentSizeInChars" and "maxOverlapSizeInChars",
   *                          which are extracted and stored in their respective fields.
   *                          Remaining entries are stored in {@code otherAttributes}.
   */
  public ChunkResponseAttributes(HashMap<String, Object> requestAttributes) {
    this.maxSegmentSizeInChars = requestAttributes.containsKey("maxSegmentSizeInChars") ? 
        (Integer) requestAttributes.remove("maxSegmentSizeInChars") : null;
    this.maxOverlapSizeInChars = requestAttributes.containsKey("maxOverlapSizeInChars") ? 
        (Integer) requestAttributes.remove("maxOverlapSizeInChars") : null;
    this.otherAttributes = requestAttributes;
  }

  /**
   * Gets the maximum segment size in characters.
   *
   * @return the maximum segment size, or {@code null} if not available.
   */
  public Integer getMaxSegmentSizeInChars() {
    return maxSegmentSizeInChars;
  }

  /**
   * Gets the maximum overlap size in characters.
   *
   * @return the maximum overlap size, or {@code null} if not available.
   */
  public Integer getMaxOverlapSizeInChars() {
    return maxOverlapSizeInChars;
  }

  /**
   * Gets additional attributes of the parser operation.
   * <p>
   * These are attributes not explicitly defined in this class.
   *
   * @return a map of additional attributes.
   */
  @MediaType(value = APPLICATION_JSON, strict = false)
  public Map<String, Object> getOtherAttributes() {
    return otherAttributes;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ChunkResponseAttributes that = (ChunkResponseAttributes) o;

    if (maxSegmentSizeInChars != null ? !maxSegmentSizeInChars.equals(that.maxSegmentSizeInChars) : that.maxSegmentSizeInChars != null) return false;
    if (maxOverlapSizeInChars != null ? !maxOverlapSizeInChars.equals(that.maxOverlapSizeInChars) : that.maxOverlapSizeInChars != null) return false;
    return otherAttributes != null ? otherAttributes.equals(that.otherAttributes) : that.otherAttributes == null;
  }

  public int hashCode() {
    int result = maxSegmentSizeInChars != null ? maxSegmentSizeInChars.hashCode() : 0;
    result = 31 * result + (maxOverlapSizeInChars != null ? maxOverlapSizeInChars.hashCode() : 0);
    result = 31 * result + (otherAttributes != null ? otherAttributes.hashCode() : 0);
    return result;
  }
} 
