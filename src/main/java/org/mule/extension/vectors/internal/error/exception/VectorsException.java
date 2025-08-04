package org.mule.extension.vectors.internal.error.exception;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;

public class VectorsException extends ModuleException {

  public <T extends Enum<T>> VectorsException(String message, ErrorTypeDefinition<T> errorTypeDefinition) {
    super(message, errorTypeDefinition);
  }

  public <T extends Enum<T>> VectorsException(String message, ErrorTypeDefinition<T> errorTypeDefinition, Throwable cause) {
    super(message, errorTypeDefinition, cause);
  }
}
