package org.mule.extension.vectors.internal.model.multimodal;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;

import java.util.List;

/**
 * Interface for models that generate embeddings for multimodal inputs (e.g., text, images, or both).
 */
public interface EmbeddingMultimodalModel {

  Integer dimension();

  /**
   * Generates embeddings for a given text input.
   *
   * @param text The text input.
   * @return The embedding of the text.
   */
  Response<Embedding> embedText(String text);

  /**
   * Generates embeddings for a given image input.
   *
   * @param imageBytes The image input as a byte array.
   * @return The embedding of the image.
   */
  Response<Embedding> embedImage(byte[] imageBytes);

  /**
   * Generates embeddings for a combination of text and image inputs.
   *
   * @param text       The text input.
   * @param imageBytes The image input as a byte array.
   * @return The embedding for the combined input.
   */
  Response<Embedding> embedTextAndImage(String text, byte[] imageBytes);

  /**
   * Generates embeddings for a batch of text inputs.
   *
   * @param texts A list of text inputs.
   * @return A list of embeddings for the texts.
   */
  Response<List<Embedding>> embedTexts(List<String> texts);

  /**
   * Generates embeddings for a batch of image inputs.
   *
   * @param imageBytesList A list of image inputs as byte arrays.
   * @return A list of embeddings for the images.
   */
  Response<List<Embedding>> embedImages(List<byte[]> imageBytesList);
}
