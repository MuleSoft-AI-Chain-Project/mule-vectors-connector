package org.mule.extension.vectors.internal.service.embeddings;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;

import java.util.List;

public interface EmbeddingService {
  Response<List<Embedding>> embedTexts(List<TextSegment> textSegments);

}
