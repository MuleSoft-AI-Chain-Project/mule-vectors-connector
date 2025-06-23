package org.mule.extension.vectors.api.metadata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.mule.runtime.extension.api.annotation.param.MediaType;

import java.io.Serializable;
import java.util.Map;

import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

/**
 * Represents the attributes of a storage operation response.
 * <p>
 * This class contains metadata about a file, such as its
 * context path, and any additional attributes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StorageResponseAttributes implements Serializable {

  private final String path;

  private final String fileName;

  /**
   * Additional attributes not explicitly defined as fields in this class.
   */
  private final Map<String, Object> otherAttributes;

  /**
   * Constructs a {@code DocumentResponseAttributes} instance.
   *
   * @param requestAttributes a map containing storage operation attributes.
   *                          Expected keys include "fileType" and "contextPath",
   *                          which are extracted and stored in their respective fields.
   *                          Remaining entries are stored in {@code otherAttributes}.
   */
  public StorageResponseAttributes(Map<String, Object> requestAttributes) {

    this.path = requestAttributes.containsKey("path") ? (String) requestAttributes.remove("path") : null;
    this.fileName = requestAttributes.containsKey("fileName") ? (String) requestAttributes.remove("fileName") : null;
    this.otherAttributes = requestAttributes;
  }

  public String getPath() {
    return path;
  }

  public String getFileName() {
    return fileName;
  }

  /**
   * Gets additional attributes of the file.
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
