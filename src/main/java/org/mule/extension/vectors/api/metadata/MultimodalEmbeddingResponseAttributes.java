package org.mule.extension.vectors.api.metadata;

import java.util.HashMap;

/**
 * Represents the attributes of an embedding operation response.
 * <p>
 * This class contains metadata about an embedding response, including the embedding model name,
 * its dimensions, and additional attributes.
 */
public class MultimodalEmbeddingResponseAttributes extends EmbeddingResponseAttributes {

  private String mimeType;
  private String mediaType;

  /**
   * Constructs an {@code EmbeddingResponseAttributes} instance.
   *
   * @param requestAttributes a map containing attributes of the embedding operation response.
   *                          Expected keys include "embeddingModelName" and "embeddingModelDimension",
   *                          which are extracted and stored in their respective fields.
   *                          Remaining entries are stored in {@code otherAttributes}.
   */
  public MultimodalEmbeddingResponseAttributes(HashMap<String, Object> requestAttributes) {

    super(requestAttributes);
    this.mimeType = requestAttributes.containsKey("mimeType") ? (String) requestAttributes.remove("mimeType") : null;
    this.mediaType = requestAttributes.containsKey("mediaType") ? (String) requestAttributes.remove("mediaType") : null;
    otherAttributes = requestAttributes;
  }

  /**
   * Gets the media type of the embedding model.
   *
   * @return the media type, or {@code null} if not available.
   */
  public String getMediaType() {
    return mediaType;
  }

  /**
   * Gets the mime type of the embedding model.
   *
   * @return the mime type, or {@code null} if not available.
   */
  public String getMimeType() {
    return mimeType;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    MultimodalEmbeddingResponseAttributes that = (MultimodalEmbeddingResponseAttributes) o;

    if (mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null) return false;
    return mediaType != null ? mediaType.equals(that.mediaType) : that.mediaType == null;
  }

  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
    result = 31 * result + (mediaType != null ? mediaType.hashCode() : 0);
    return result;
  }
}
