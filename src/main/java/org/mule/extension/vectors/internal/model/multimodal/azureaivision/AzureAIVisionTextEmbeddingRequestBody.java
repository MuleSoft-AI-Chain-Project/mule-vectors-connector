package org.mule.extension.vectors.internal.model.multimodal.azureaivision;

public class AzureAIVisionTextEmbeddingRequestBody {

  private String text;

  public AzureAIVisionTextEmbeddingRequestBody(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return "AzureAIVisionTextEmbeddingRequestBody{" +
        "text='" + text + '\'' +
        '}';
  }
}
