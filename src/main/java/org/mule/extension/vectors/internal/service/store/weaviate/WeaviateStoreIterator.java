package org.mule.extension.vectors.internal.service.store.weaviate;

import org.mule.extension.vectors.internal.helper.parameter.QueryParameters;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;
import org.mule.extension.vectors.internal.service.store.VectoreStoreIterator;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeaviateStoreIterator<Embedded> implements VectoreStoreIterator<VectorStoreRow<Embedded>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WeaviateStoreIterator.class);


  public WeaviateStoreIterator(
                               QueryParameters queryParams) {}

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public VectorStoreRow<Embedded> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    try {
      return null;
    } catch (Exception e) {
      LOGGER.error("Error while fetching next row", e);
      throw new NoSuchElementException("No more elements available");
    }
  }
}
