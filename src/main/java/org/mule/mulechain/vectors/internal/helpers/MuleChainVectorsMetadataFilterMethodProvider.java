package org.mule.mulechain.vectors.internal.helpers;

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
public class MuleChainVectorsMetadataFilterMethodProvider implements ValueProvider {

  public static final String IS_EQUAL_TO = "isEqualTo";
  public static final String IS_NOT_EQUAL_TO = "isNotEqualTo";

  /**
   * Resolves and returns a set of filter methods for metadata filtering.
   *
   * @return A {@link Set} of {@link Value} objects representing available filter methods,
   *         including "isEqualTo" and "isNotEqualTo".
   * @throws ValueResolvingException if an error occurs while resolving the values.
   */
  @Override
  public Set<Value> resolve() throws ValueResolvingException {
    return ValueBuilder.getValuesFor(IS_EQUAL_TO, IS_NOT_EQUAL_TO); // Additional methods can be added as needed
  }

}