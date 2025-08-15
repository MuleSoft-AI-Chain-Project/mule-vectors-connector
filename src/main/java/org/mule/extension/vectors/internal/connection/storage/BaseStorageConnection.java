package org.mule.extension.vectors.internal.connection.storage;

import org.mule.extension.vectors.internal.connection.provider.ConnectorConnection;

public interface BaseStorageConnection extends ConnectorConnection {

  String getStorageType();



}
