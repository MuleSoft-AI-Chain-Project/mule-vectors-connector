package org.mule.extension.vectors.internal.connection;

public interface ConnectorConnection {
  void disconnect();

  void validate();
}
