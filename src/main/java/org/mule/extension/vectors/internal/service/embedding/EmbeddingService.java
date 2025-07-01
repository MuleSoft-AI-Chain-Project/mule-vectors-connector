package org.mule.extension.vectors.internal.service.embedding;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;

import java.util.List;

public interface EmbeddingService {
  Response<List<Embedding>> embedTexts(List<TextSegment> textSegments);
  Response<Embedding> embedImage(byte[] imageBytes);
  Response<Embedding> embedTextAndImage(String text, byte[] imageBytes);

}
