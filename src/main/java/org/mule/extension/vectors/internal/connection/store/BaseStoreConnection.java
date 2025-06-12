package org.mule.extension.vectors.internal.connection.store;

import com.mulesoft.connectors.commons.template.connection.ConnectorConnection;

public interface BaseStoreConnection extends ConnectorConnection {

  String getVectorStore();

  BaseStoreConnectionParameters getConnectionParameters();
}
