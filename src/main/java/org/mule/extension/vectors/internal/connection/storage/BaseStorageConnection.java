package org.mule.extension.vectors.internal.connection.storage;

import com.mulesoft.connectors.commons.template.connection.ConnectorConnection;
import org.mule.runtime.api.connection.ConnectionException;

public interface BaseStorageConnection extends ConnectorConnection {

  String getStorageType();



}
