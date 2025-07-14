package org.mule.extension.vectors.internal.service.store.weaviate;

import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

public class WeaviateStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeaviateStoreIterator.class);

  private final QueryParameters queryParams;


  public WeaviateStoreIterator(
      // WeaviateClient weaviateClient,
      QueryParameters queryParams
      // ...other params as needed
  ) {
    // this.weaviateClient = weaviateClient;
    this.queryParams = queryParams;
    // this.currentBatch = new ArrayList<>();
    // this.currentIndex = 0;
    // this.hasMorePages = true;
    // fetchNextBatch();
  }

  @Override
  public boolean hasNext() {
    // TODO: Implement actual paging logic using Weaviate client
    // return currentIndex < currentBatch.size() || fetchNextBatch();
    return false;
  }

  @Override
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    try {
      // TODO: Fetch the next result from Weaviate and convert to VectorStoreRow<Embedded>
      // Example (replace with actual logic):
      // WeaviateResultType result = currentBatch.get(currentIndex++);
      // String id = ...;
      // float[] vector = ...;
      // String text = ...;
      // Map<String, Object> metadataMap = ...;
      // @SuppressWarnings("unchecked")
      // Embedded embedded = (Embedded) new TextSegment(text, Metadata.from(metadataMap));
      // return new VectorStoreRow<>(id, vector != null ? new Embedding(vector) : null, embedded);
      return null;
    } catch (Exception e) {
      LOGGER.error("Error while fetching next row", e);
      throw new NoSuchElementException("No more elements available");
    }
  }

  // private boolean fetchNextBatch() {
  //     // TODO: Implement batch fetching logic using Weaviate client
  //     return false;
  // }
}
