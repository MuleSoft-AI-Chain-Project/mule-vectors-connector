package org.mule.extension.vectors.internal.connection.model;

import com.mulesoft.connectors.commons.template.connection.ConnectorConnection;


public interface BaseModelConnection extends ConnectorConnection {

  String getEmbeddingModelService();
}
