package org.mule.extension.vectors.internal.helper.model;

public class EmbeddingModelHelper {

  public enum EmbeddingModelType {
    TEXT("Text Embedding Model"),
    MULTIMODAL("Multimodal Embedding Model");

    private final String description;

    EmbeddingModelType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  public enum TextEmbeddingModelNames {
    TEXT_EMBEDDING_3_SMALL("text-embedding-3-small"),
    TEXT_EMBEDDING_3_LARGE("text-embedding-3-large"),
    TEXT_EMBEDDING_ADA_002("text-embedding-ada-002"),
    MISTRAL_EMBED("mistral-embed"),
    NOMIC_EMBED_TEXT_V1("nomic-embed-text-v1"),
    NOMIC_EMBED_TEXT_V1_5("nomic-embed-text-v1.5"),
    OLLAMA_NOMIC_EMBED_TEXT("nomic-embed-text"),
    OLLAMA_EMBED_LARGE("mxbai-embed-large"),
    OLLAMA_SNOWFLAKE_ARCTIC_EMBED("snowflake-arctic-embed"),
    OLLAMA_SNOWFLAKE_ARCTIC_EMBED2("snowflake-arctic-embed2"),
    OLLAMA_BGE_M3("bge-m3"),
    OLLAMA_BGE_LARGE("bge-large"),
    OLLAMA_ALL_MINILM("all-minilm"),
    OLLAMA_PARAPHRASE_MULTILINGUAL("paraphrase-multilingual"),
    OLLAMA_GRANITE_EMBEDDING("granite-embedding"),
    FALCON_7B_INSTRUCT("tiiuae/falcon-7b-instruct"),
    MINI_LM_L6_V2("sentence-transformers/all-MiniLM-L6-v2"),
    SFDC_TEXT_EMBEDDING_ADA_002("sfdc_ai__DefaultTextEmbeddingAda_002"),
    SFDC_AZURE_TEXT_EMBEDDING_ADA_002("sfdc_ai__DefaultAzureOpenAITextEmbeddingAda_002"),
    SFDC_OPENAI_TEXT_EMBEDDING_ADA_002("sfdc_ai__DefaultOpenAITextEmbeddingAda_002"),
    VERTEX_TEXT_EMBEDDING_GECKO_003("textembedding-gecko@003"),
    VERTEX_TEXT_EMBEDDING_004("text-embedding-004"),
    VERTEX_TEXT_EMBEDDING_GECKO_MULTILINGUAL_001("textembedding-gecko-multilingual@001"),
    VERTEX_TEXT_MULTILINGUAL_EMBEDDING_002("text-multilingual-embedding-002");

    private final String modelName;

    TextEmbeddingModelNames(String modelName) {
      this.modelName = modelName;
    }

    public String getModelName() {
      return modelName;
    }
  }

  public enum MultimodalEmbeddingModelNames {
    VERTEX_MULTI_MODAL_EMBEDDING("multimodalembedding"),
    NOMIC_EMBED_VISION_V1("nomic-embed-vision-v1"),
    NOMIC_EMBED_VISION_V1_5("nomic-embed-vision-v1.5"),
    AZURE_AI_VISION_2022_04_11("2022-04-11"),
    AZURE_AI_VISION_2023_04_15("2023-04-15");

    private final String modelName;

    MultimodalEmbeddingModelNames(String modelName) {
      this.modelName = modelName;
    }

    public String getModelName() {
      return modelName;
    }
  }

  public static EmbeddingModelType getModelType(String embeddingModelName) {
    if (embeddingModelName == null || embeddingModelName.isEmpty()) {
      return null; // or throw an exception if required
    }

    // Check if the model is in TextEmbeddingModels
    if (isTextEmbeddingModel(embeddingModelName)) {
      return EmbeddingModelType.TEXT;
    }

    // Check if the model is in MultimodalEmbeddingModels
    if (isMultimodalEmbeddingModel(embeddingModelName)) {
      return EmbeddingModelType.MULTIMODAL;
    }

    return null; // or throw an exception if the model name is not recognized
  }

  // Helper method to check if the model is in TextEmbeddingModels
  private static boolean isTextEmbeddingModel(String modelName) {
    for (TextEmbeddingModelNames model : TextEmbeddingModelNames.values()) {
      if (model.getModelName().equals(modelName)) {
        return true;
      }
    }
    return false;
  }

  // Helper method to check if the model is in MultimodalEmbeddingModels
  private static boolean isMultimodalEmbeddingModel(String modelName) {
    for (MultimodalEmbeddingModelNames model : MultimodalEmbeddingModelNames.values()) {
      if (model.getModelName().equals(modelName)) {
        return true;
      }
    }
    return false;
  }
}

