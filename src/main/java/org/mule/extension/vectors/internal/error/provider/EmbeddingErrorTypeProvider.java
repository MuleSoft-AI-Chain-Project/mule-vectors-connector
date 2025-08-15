/**
 * (c) 2003-2024 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.vectors.internal.error.provider;

import static org.mule.extension.vectors.internal.error.MuleVectorsErrorType.AI_SERVICES_FAILURE;
import static org.mule.extension.vectors.internal.error.MuleVectorsErrorType.AI_SERVICES_RATE_LIMITING_ERROR;
import static org.mule.extension.vectors.internal.error.MuleVectorsErrorType.EMBEDDING_OPERATIONS_FAILURE;
import static org.mule.extension.vectors.internal.error.MuleVectorsErrorType.INVALID_CONNECTION;
import static org.mule.extension.vectors.internal.error.MuleVectorsErrorType.INVALID_PARAMETER;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

public class EmbeddingErrorTypeProvider implements ErrorTypeProvider {

  @SuppressWarnings("rawtypes")
  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    return unmodifiableSet(new HashSet<>(asList(
                                                INVALID_CONNECTION,
                                                INVALID_PARAMETER,
                                                EMBEDDING_OPERATIONS_FAILURE,
                                                AI_SERVICES_RATE_LIMITING_ERROR,
                                                AI_SERVICES_FAILURE)));
  }
}
