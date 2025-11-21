package org.mule.extension.vectors.api.metadata;

import java.io.Serializable;
import java.util.Map;

import org.mule.runtime.extension.api.annotation.param.MediaType;
import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

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
  private final String mimeType;

  private transient final Map<String, Object> metadata;
  /**
   * Additional attributes not explicitly defined as fields in this class.
   */
  private transient final Map<String, Object> otherAttributes;

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
    this.mimeType = requestAttributes.containsKey("mimeType") ? (String) requestAttributes.remove("mimeType") : null;
    this.metadata = requestAttributes.containsKey("metadata") ? (Map<String, Object>) requestAttributes.remove("metadata") : null;
    this.otherAttributes = requestAttributes;
  }

  public String getPath() {
    return path;
  }

  public String getFileName() {
    return fileName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    StorageResponseAttributes that = (StorageResponseAttributes) o;
    return java.util.Objects.equals(path, that.path)
        && java.util.Objects.equals(fileName, that.fileName)
        && java.util.Objects.equals(mimeType, that.mimeType)
        && java.util.Objects.equals(metadata, that.metadata)
        && java.util.Objects.equals(otherAttributes, that.otherAttributes);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(path, fileName, mimeType, metadata, otherAttributes);
  }
}
