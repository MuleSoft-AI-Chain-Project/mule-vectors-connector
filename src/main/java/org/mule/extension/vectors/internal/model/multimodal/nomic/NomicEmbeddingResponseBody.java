package org.mule.extension.vectors.internal.model.multimodal.nomic;

import java.util.List;

public class NomicEmbeddingResponseBody {
  private List<float[]> embeddings;
  private Usage usage;

  NomicEmbeddingResponseBody() {
  }

  public List<float[]> getEmbeddings() {
    return this.embeddings;
  }

  public Usage getUsage() {
    return this.usage;
  }

  public static class Usage {

    private Integer totalTokens;

    Usage() {
    }

    public Integer getTotalTokens() {
      return this.totalTokens;
    }
  }
}
