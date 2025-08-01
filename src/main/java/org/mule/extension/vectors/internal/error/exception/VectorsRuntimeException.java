package org.mule.extension.vectors.internal.error.exception;

import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.i18n.I18nMessage;

public class VectorsRuntimeException extends MuleRuntimeException {

  public VectorsRuntimeException(I18nMessage message) {
    super(message);
  }

  public VectorsRuntimeException(I18nMessage message, Throwable cause) {
    super(message, cause);
  }

  public VectorsRuntimeException(Throwable cause) {
    super(cause);
  }
}
