package org.mule.extension.vectors.internal.helper.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper.EmbeddingModelType;
import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper.MultimodalEmbeddingModelNames;
import org.mule.extension.vectors.internal.helper.model.EmbeddingModelHelper.TextEmbeddingModelNames;

import org.junit.jupiter.api.Test;

class EmbeddingModelHelperTest {

  @Test
  void getModelType_null_returnsNull() {
    assertThat(EmbeddingModelHelper.getModelType(null)).isNull();
  }

  @Test
  void getModelType_empty_returnsNull() {
    assertThat(EmbeddingModelHelper.getModelType("")).isNull();
  }

  @Test
  void getModelType_textModel_returnsText() {
    assertThat(EmbeddingModelHelper.getModelType("text-embedding-3-small"))
        .isEqualTo(EmbeddingModelType.TEXT);
  }

  @Test
  void getModelType_multimodalModel_returnsMultimodal() {
    assertThat(EmbeddingModelHelper.getModelType("multimodalembedding"))
        .isEqualTo(EmbeddingModelType.MULTIMODAL);
  }

  @Test
  void getModelType_unknown_returnsNull() {
    assertThat(EmbeddingModelHelper.getModelType("unknown-model")).isNull();
  }

  @Test
  void embeddingModelType_descriptions() {
    assertThat(EmbeddingModelType.TEXT.getDescription()).isEqualTo("Text Embedding Model");
    assertThat(EmbeddingModelType.MULTIMODAL.getDescription()).isEqualTo("Multimodal Embedding Model");
  }

  @Test
  void textEmbeddingModelNames_getModelName() {
    assertThat(TextEmbeddingModelNames.TEXT_EMBEDDING_3_SMALL.getModelName())
        .isEqualTo("text-embedding-3-small");
    assertThat(TextEmbeddingModelNames.TEXT_EMBEDDING_3_LARGE.getModelName())
        .isEqualTo("text-embedding-3-large");
    assertThat(TextEmbeddingModelNames.MISTRAL_EMBED.getModelName())
        .isEqualTo("mistral-embed");
    assertThat(TextEmbeddingModelNames.VERTEX_TEXT_MULTILINGUAL_EMBEDDING_002.getModelName())
        .isEqualTo("text-multilingual-embedding-002");
  }

  @Test
  void multimodalEmbeddingModelNames_getModelName() {
    assertThat(MultimodalEmbeddingModelNames.VERTEX_MULTI_MODAL_EMBEDDING.getModelName())
        .isEqualTo("multimodalembedding");
    assertThat(MultimodalEmbeddingModelNames.NOMIC_EMBED_VISION_V1.getModelName())
        .isEqualTo("nomic-embed-vision-v1");
    assertThat(MultimodalEmbeddingModelNames.AZURE_AI_VISION_2023_04_15.getModelName())
        .isEqualTo("2023-04-15");
  }
}
