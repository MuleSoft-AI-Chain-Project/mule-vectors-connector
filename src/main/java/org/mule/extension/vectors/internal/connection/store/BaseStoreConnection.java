package org.mule.extension.vectors.internal.connection.store;

import org.mule.runtime.api.connection.ConnectionException;

public interface BaseStoreConnection {

  String getVectorStore();

  void connect() throws ConnectionException;

  void disconnect();

  boolean isValid();
}
