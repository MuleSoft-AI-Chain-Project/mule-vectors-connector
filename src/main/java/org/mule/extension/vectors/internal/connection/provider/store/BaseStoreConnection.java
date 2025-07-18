package org.mule.extension.vectors.internal.connection.provider.store;

import org.mule.extension.vectors.internal.connection.provider.ConnectorConnection;

public interface BaseStoreConnection extends ConnectorConnection {

  String getVectorStore();

  BaseStoreConnectionParameters getConnectionParameters();
}
