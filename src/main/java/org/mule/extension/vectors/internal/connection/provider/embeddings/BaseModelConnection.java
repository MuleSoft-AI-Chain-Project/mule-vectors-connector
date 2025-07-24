package org.mule.extension.vectors.internal.connection.provider.embeddings;

import org.mule.extension.vectors.internal.connection.provider.ConnectorConnection;

public interface BaseModelConnection extends ConnectorConnection {

  String getEmbeddingModelService();
}
