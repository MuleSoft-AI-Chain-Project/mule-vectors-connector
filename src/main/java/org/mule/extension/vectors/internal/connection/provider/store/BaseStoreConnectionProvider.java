package org.mule.extension.vectors.internal.connection.provider.store;

import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;

public interface BaseStoreConnectionProvider
    extends CachedConnectionProvider<BaseStoreConnection>, Initialisable, Disposable {

  default void disconnect(BaseStoreConnection connection) {
    connection.disconnect();
  }

  default ConnectionValidationResult validate(BaseStoreConnection connection) {
    try {
      connection.validate();
      return ConnectionValidationResult.success();
    } catch (Exception e) {
      return ConnectionValidationResult.failure(e.getMessage(), e);
    }
  }

}
