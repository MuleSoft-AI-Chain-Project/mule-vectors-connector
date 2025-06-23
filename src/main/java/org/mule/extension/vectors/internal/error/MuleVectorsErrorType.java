package org.mule.extension.vectors.internal.error;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;

import java.util.Optional;


/**
 * Defines the custom error types for the Vectors Connector.
 * Each error is mapped to a parent {@link ErrorTypeDefinition} from the Mule SDK to enable more flexible error handling strategies in Mule flows.
 *
 * (c) 2003-2024 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
public enum MuleVectorsErrorType implements ErrorTypeDefinition<MuleVectorsErrorType> {

  INVALID_PARAMETERS_ERROR,
  COMPOSITE_OPERATIONS_FAILURE,
  DOCUMENT_PARSING_FAILURE,
  DOCUMENT_OPERATIONS_FAILURE,
  MEDIA_OPERATIONS_FAILURE,
  EMBEDDING_OPERATIONS_FAILURE,
  AI_SERVICES_FAILURE,
  AI_SERVICES_RATE_LIMITING_ERROR,
  STORAGE_SERVICES_FAILURE,
  STORE_UNSUPPORTED_OPERATION,
  STORE_OPERATIONS_FAILURE,
  STORE_SERVICES_FAILURE,
  STORE_CONNECTION_FAILURE}
