package org.mule.extension.vectors.internal.helper.provider;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;
import org.mule.runtime.extension.api.values.ValueResolvingException;

import java.util.Set;

/**
 * Provides a set of available filter methods for MuleChain Vectors metadata filtering.
 * Implements {@link ValueProvider} to supply a predefined set of values that can be used
 * as options for filtering operations, such as "isEqualTo" and "isNotEqualTo".
 */
public class MetadataFilterMethodProvider implements ValueProvider {

  /**
   * Resolves and returns a set of filter methods for metadata filtering.
   *
   * @return A {@link Set} of {@link Value} objects representing available filter methods,
   *         including "isEqualTo" and "isNotEqualTo".
   * @throws ValueResolvingException if an error occurs while resolving the values.
   */
  @Override
  public Set<Value> resolve() throws ValueResolvingException {

    return ValueBuilder.getValuesFor(
                                     Constants.METADATA_FILTER_METHOD_IS_EQUAL_TO,
                                     Constants.METADATA_FILTER_METHOD_IS_NOT_EQUAL_TO,
                                     Constants.METADATA_FILTER_METHOD_IS_GREATER_THAN,
                                     Constants.METADATA_FILTER_METHOD_IS_LESS_THAN); // Additional methods can be added as needed
  }

}
