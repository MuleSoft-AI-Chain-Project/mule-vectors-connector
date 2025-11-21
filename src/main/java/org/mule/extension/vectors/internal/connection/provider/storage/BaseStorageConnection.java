package org.mule.extension.vectors.internal.connection.provider.storage;

import org.mule.extension.vectors.internal.connection.provider.ConnectorConnection;

public interface BaseStorageConnection extends ConnectorConnection {

  String getStorageType();



}
