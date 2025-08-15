package org.mule.extension.vectors.internal.connection.storage;

import org.mule.extension.vectors.internal.connection.storage.BaseStorageConnection;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;

public interface BaseStorageConnectionProvider
    extends CachedConnectionProvider<BaseStorageConnection>, Initialisable, Disposable {

  default void disconnect(BaseStorageConnection connection) {
    connection.disconnect();
  }

  default ConnectionValidationResult validate(BaseStorageConnection connection) {
    try {
      connection.validate();
      return ConnectionValidationResult.success();
    } catch (Exception e) {
      return ConnectionValidationResult.failure(e.getMessage(), e);
    }
  }

}
