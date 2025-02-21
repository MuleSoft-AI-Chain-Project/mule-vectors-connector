package org.mule.extension.vectors.api.metadata;

import org.mule.runtime.extension.api.annotation.param.MediaType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_JSON;

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
  }
}
