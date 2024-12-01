package org.mule.extension.vectors.internal.model;

import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public interface BaseModelConnection {

  String getEmbeddingModelService();
}
