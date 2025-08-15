package org.mule.extension.vectors.internal.helper.validation;

import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * Simple parameter validation utility.
 * Focuses only on validating connection parameters.
 * Uses static methods for optimal performance.
 */
public class ParameterValidator {

  // Private constructor to prevent instantiation
  private ParameterValidator() {}

  // ============ STATIC METHODS (No Instance Creation) ============

  /**
   * Static method to validate that a string parameter is not null or blank.
   */
  public static void requireNotBlank(String connectionType, String paramName, String value) {
    if (value == null || value.isBlank()) {
      throwValidationException(connectionType, paramName + " is required for " + connectionType + " connection");
    }
  }

  /**
   * Static method to validate that a parameter is not null.
   */
  public static void requireNotNull(String connectionType, String paramName, Object value) {
    if (value == null) {
      throwValidationException(connectionType, paramName + " is required for " + connectionType + " connection");
    }
  }

  /**
   * Static method to validate that an integer parameter is positive.
   */
  public static void requirePositive(String connectionType, String paramName, int value) {
    if (value <= 0) {
      throwValidationException(connectionType, paramName + " is required for " + connectionType + " connection and must be > 0");
    }
  }

  /**
   * Static method to validate that either one of two parameters is provided.
   */
  public static void requireEither(String connectionType, String param1Name, String param1Value,
                                   String param2Name, String param2Value) {
    if ((param1Value == null || param1Value.isBlank()) &&
        (param2Value == null || param2Value.isBlank())) {
      throwValidationException(connectionType, "Either " + param1Name + " or " + param2Name +
          " is required for " + connectionType + " connection");
    }
  }

  // ============ HELPER METHODS ============

  private static void throwValidationException(String connectionType, String message) {
    throw new ModuleException(message, MuleVectorsErrorType.STORE_CONNECTION_FAILURE);
  }
}
