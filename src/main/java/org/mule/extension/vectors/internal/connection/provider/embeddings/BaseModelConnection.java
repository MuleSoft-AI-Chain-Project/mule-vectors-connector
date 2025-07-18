package org.mule.extension.vectors.internal.connection.embeddings;


import org.mule.extension.vectors.internal.connection.ConnectorConnection;

public interface BaseModelConnection extends ConnectorConnection {

  String getEmbeddingModelService();
}
