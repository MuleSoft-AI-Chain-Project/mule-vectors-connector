package org.mule.extension.vectors.internal.error;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

/**
 * (c) 2003-2024 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
public enum MuleVectorsErrorType implements ErrorTypeDefinition<MuleVectorsErrorType> {

  INVALID_PARAMETERS_ERROR,
  COMPOSITE_OPERATIONS_FAILURE,
  DOCUMENT_PARSING_FAILURE,
  DOCUMENT_OPERATIONS_FAILURE,
  MEDIA_OPERATIONS_FAILURE,
  EMBEDDING_OPERATIONS_FAILURE,
  STORE_OPERATIONS_FAILURE,
  AI_SERVICES_FAILURE,
  AI_SERVICES_RATE_LIMITING_ERROR,
  STORE_SERVICES_FAILURE,
  STORAGE_SERVICES_FAILURE}
