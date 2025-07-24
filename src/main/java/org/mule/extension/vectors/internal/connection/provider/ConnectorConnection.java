package org.mule.extension.vectors.internal.connection.provider;

public interface ConnectorConnection {

  void disconnect();

  void validate();
}
