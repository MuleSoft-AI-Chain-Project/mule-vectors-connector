package org.mule.extension.vectors.internal.service;

public interface VectorStoreServiceProvider {
  public VectorStoreService getService();
  VectoreStoreIterator getFileIterator();

}
