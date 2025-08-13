package org.mule.extension.vectors.internal.service.embeddings;

import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;

public interface EmbeddingService {

  Response<List<Embedding>> embedTexts(List<String> inputs);

  Response<Embedding> embedImage(byte[] imageBytes);

  Response<Embedding> embedTextAndImage(String text, byte[] imageBytes);
}
