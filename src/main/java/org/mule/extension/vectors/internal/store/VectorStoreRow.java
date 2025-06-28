package org.mule.extension.vectors.internal.store;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.internal.ValidationUtils;
import org.apache.poi.ss.usermodel.Row;

import java.util.Objects;

public class VectorStoreRow<Embedded> {
  private final String id;
  private final Embedding embedding;
  private final Embedded embedded;


  public VectorStoreRow(String id, Embedding embedding) {
    this(id, embedding, (Embedded)null);
  }

  public VectorStoreRow(String id, Embedding embedding, Embedded embedded) {
    this.id = ValidationUtils.ensureNotBlank(id, "id");
    this.embedding = embedding;
    this.embedded = embedded;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && this.getClass() == o.getClass()) {
      VectorStoreRow that = (VectorStoreRow)o;
      return Objects.equals(this.id, that.id) && Objects.equals(this.embedding, that.embedding) && Objects.equals(this.embedded, that.embedded);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return Objects.hash(new Object[]{this.id, this.embedding, this.embedded});
  }

  public String getId() {
    return id;
  }

  public Embedding getEmbedding() {
    return embedding;
  }

  public Embedded getEmbedded() {
    return embedded;
  }
}
