package org.mule.extension.vectors.internal.connection.model;

import org.mule.runtime.api.connection.ConnectionException;

public interface BaseModelConnection {

  String getEmbeddingModelService();

  void connect() throws ConnectionException;

  void disconnect();

  boolean isValid();
}
