package org.mule.extension.vectors.internal.service;

import org.mule.extension.vectors.internal.storage.FileIterator;

public interface VectorStoreServiceProvider {
  public VectorStoreService getService();
  VectoreStoreIterator getFileIterator();

}
