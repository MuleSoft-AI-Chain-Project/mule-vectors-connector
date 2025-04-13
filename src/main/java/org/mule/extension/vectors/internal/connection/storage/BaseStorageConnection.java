package org.mule.extension.vectors.internal.connection.storage;

import org.mule.runtime.api.connection.ConnectionException;

public interface BaseStorageConnection {

  String getStorageType();

  void connect() throws ConnectionException;

  void disconnect();

  boolean isValid();
}
