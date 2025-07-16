package org.mule.extension.vectors.internal.connection.store;

import org.mule.extension.vectors.internal.connection.ConnectorConnection;

public interface BaseStoreConnection extends ConnectorConnection {

  String getVectorStore();

  BaseStoreConnectionParameters getConnectionParameters();
}
