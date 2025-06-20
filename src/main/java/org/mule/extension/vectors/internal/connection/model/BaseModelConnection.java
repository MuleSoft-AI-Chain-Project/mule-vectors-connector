package org.mule.extension.vectors.internal.connection.model;

import com.mulesoft.connectors.commons.template.connection.ConnectorConnection;
import org.mule.runtime.api.connection.ConnectionException;

public interface BaseModelConnection extends ConnectorConnection {

  String getEmbeddingModelService();
}
