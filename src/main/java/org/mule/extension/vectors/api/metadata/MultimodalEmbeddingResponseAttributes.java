package org.mule.extension.vectors.api.metadata;

import java.util.HashMap;

/**
 * Represents the attributes of an embedding operation response.
 * <p>
 * This class contains metadata about an embedding response, including the embedding model name,
 * its dimensions, and additional attributes.
 */
public class MultimodalEmbeddingResponseAttributes extends EmbeddingResponseAttributes {

  private String filename;
  private String mimeType;

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
    this.filename = requestAttributes.containsKey("filename") ? (String) requestAttributes.remove("filename") : null;
    this.mimeType = requestAttributes.containsKey("mimeType") ? (String) requestAttributes.remove("mimeType") : null;
    otherAttributes = requestAttributes;
  }

  /**
   * Gets the name of the embedding model.
   *
   * @return the embedding model name, or {@code null} if not available.
   */
  public String getFilename() {
    return filename;  
  }

  /**
   * Gets the mime type of the embedding model.
   *
   * @return the mime type, or {@code null} if not available.
   */
  public String getMimeType() {
    return mimeType;
  }
}
