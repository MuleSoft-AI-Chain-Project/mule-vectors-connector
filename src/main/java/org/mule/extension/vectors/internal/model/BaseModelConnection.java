package org.mule.extension.vectors.internal.model;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class BaseModelConnection {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseModelConnection.class);

  public BaseModelConnection() {}

  public String getEmbeddingModelService() {
    return null;
  }

  public BaseModelConnection connect()  throws ConnectionException {

    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  public void disconnect(BaseModelConnection connection) {

    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  public ConnectionValidationResult validate(BaseModelConnection connection) {

    throw new UnsupportedOperationException("This method should be overridden by subclasses");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseModelConnection that = (BaseModelConnection) o;
    return Objects.equals(getEmbeddingModelService(), that.getEmbeddingModelService());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getEmbeddingModelService());
  }
}
