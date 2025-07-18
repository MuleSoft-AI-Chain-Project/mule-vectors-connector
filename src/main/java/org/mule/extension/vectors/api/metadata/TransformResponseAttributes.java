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
 * Represents the attributes of a document operation response.
 * <p>
 * This class contains metadata about a document, such as its file type,
 * context path, and any additional attributes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransformResponseAttributes implements Serializable {

  /**
   * The type of the file associated with the document.
   */
  private final String fileType;

  /**
   * The media type associated with the document (e.g., "text", "image").
   */
  private final String mediaType;

  /**
   * The MIME type of the document (e.g., "application/pdf").
   */
  private final String mimeType;


  /**
   * Additional attributes not explicitly defined as fields in this class.
   */
  private transient final HashMap<String, Object> otherAttributes;

  /**
   * Constructs a {@code TransformResponseAttributes} instance.
   *
   * @param requestAttributes a map containing document operation attributes.
   *                          Expected keys include "fileType", "mediaType", and "mimeType",
   *                          which are extracted and stored in their respective fields.
   *                          Remaining entries are stored in {@code otherAttributes}.
   */
  public TransformResponseAttributes(HashMap<String, Object> requestAttributes) {
    this.fileType = requestAttributes.containsKey("fileType") ? (String) requestAttributes.remove("fileType") : null;
    this.mediaType = requestAttributes.containsKey("mediaType") ? (String) requestAttributes.remove("mediaType") : null;
    this.mimeType = requestAttributes.containsKey("mimeType") ? (String) requestAttributes.remove("mimeType") : null;
    this.otherAttributes = requestAttributes;
  }

  /**
   * Gets the file type of the document.
   *
   * @return the file type, or {@code null} if not available.
   */
  public String getFileType() {
    return fileType;
  }

  /**
   * Gets the media type of the document.
   *
   * @return the media type, or {@code null} if not available.
   */
  public String getMediaType() {
    return mediaType;
  }

  /**
   * Gets the MIME type of the document.
   *
   * @return the MIME type, or {@code null} if not available.
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Gets additional attributes of the document.
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

    TransformResponseAttributes that = (TransformResponseAttributes) o;

    if (fileType != null ? !fileType.equals(that.fileType) : that.fileType != null) return false;
    if (mediaType != null ? !mediaType.equals(that.mediaType) : that.mediaType != null) return false;
    if (mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null) return false;
    return otherAttributes != null ? otherAttributes.equals(that.otherAttributes) : that.otherAttributes == null;
  }

  public int hashCode() {
    int result = fileType != null ? fileType.hashCode() : 0;
    result = 31 * result + (mediaType != null ? mediaType.hashCode() : 0);
    result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
    result = 31 * result + (otherAttributes != null ? otherAttributes.hashCode() : 0);
    return result;
  }
}
