package org.mule.extension.vectors.internal.service.embeddings;

import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;

public interface EmbeddingService {

  Response<List<Embedding>> embedTexts(List<TextSegment> textSegments);

}
