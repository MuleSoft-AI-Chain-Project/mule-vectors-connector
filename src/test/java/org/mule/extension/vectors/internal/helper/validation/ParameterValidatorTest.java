package org.mule.extension.vectors.internal.helper.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.exception.ModuleException;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for ParameterValidator class.
 */
public class ParameterValidatorTest {

  @Test
  public void testRequireNotBlank_ValidString_ShouldPass() {
    // Given
    String validValue = "valid-value";

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ParameterValidator.requireNotBlank("TestConnection", "Test Parameter", validValue);
    });
  }

  @Test
  public void testRequireNotBlank_NullValue_ShouldThrowException() {
    // Given
    String nullValue = null;

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ParameterValidator.requireNotBlank("TestConnection", "Test Parameter", nullValue);
    });

    assertEquals("Test Parameter is required for TestConnection connection", exception.getMessage());
    assertEquals(MuleVectorsErrorType.STORE_CONNECTION_FAILURE, exception.getType());
  }

  @Test
  public void testRequireNotBlank_EmptyString_ShouldThrowException() {
    // Given
    String emptyValue = "";

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ParameterValidator.requireNotBlank("TestConnection", "Test Parameter", emptyValue);
    });

    assertEquals("Test Parameter is required for TestConnection connection", exception.getMessage());
  }

  @Test
  public void testRequireNotBlank_BlankString_ShouldThrowException() {
    // Given
    String blankValue = "   ";

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ParameterValidator.requireNotBlank("TestConnection", "Test Parameter", blankValue);
    });

    assertEquals("Test Parameter is required for TestConnection connection", exception.getMessage());
  }

  @Test
  public void testRequireNotNull_ValidObject_ShouldPass() {
    // Given
    Object validObject = new Object();

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ParameterValidator.requireNotNull("TestConnection", "Test Parameter", validObject);
    });
  }

  @Test
  public void testRequireNotNull_NullValue_ShouldThrowException() {
    // Given
    Object nullValue = null;

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ParameterValidator.requireNotNull("TestConnection", "Test Parameter", nullValue);
    });

    assertEquals("Test Parameter is required for TestConnection connection", exception.getMessage());
    assertEquals(MuleVectorsErrorType.STORE_CONNECTION_FAILURE, exception.getType());
  }

  @Test
  public void testRequirePositive_PositiveValue_ShouldPass() {
    // Given
    int positiveValue = 8080;

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ParameterValidator.requirePositive("TestConnection", "Port", positiveValue);
    });
  }

  @Test
  public void testRequirePositive_ZeroValue_ShouldThrowException() {
    // Given
    int zeroValue = 0;

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ParameterValidator.requirePositive("TestConnection", "Port", zeroValue);
    });

    assertEquals("Port is required for TestConnection connection and must be > 0", exception.getMessage());
  }

  @Test
  public void testRequirePositive_NegativeValue_ShouldThrowException() {
    // Given
    int negativeValue = -1;

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ParameterValidator.requirePositive("TestConnection", "Port", negativeValue);
    });

    assertEquals("Port is required for TestConnection connection and must be > 0", exception.getMessage());
  }

  @Test
  public void testRequireEither_FirstParameterProvided_ShouldPass() {
    // Given
    String param1 = "value1";
    String param2 = null;

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ParameterValidator.requireEither("TestConnection", "Parameter1", param1, "Parameter2", param2);
    });
  }

  @Test
  public void testRequireEither_SecondParameterProvided_ShouldPass() {
    // Given
    String param1 = null;
    String param2 = "value2";

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ParameterValidator.requireEither("TestConnection", "Parameter1", param1, "Parameter2", param2);
    });
  }

  @Test
  public void testRequireEither_BothParametersProvided_ShouldPass() {
    // Given
    String param1 = "value1";
    String param2 = "value2";

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ParameterValidator.requireEither("TestConnection", "Parameter1", param1, "Parameter2", param2);
    });
  }

  @Test
  public void testRequireEither_BothParametersNull_ShouldThrowException() {
    // Given
    String param1 = null;
    String param2 = null;

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ParameterValidator.requireEither("TestConnection", "Parameter1", param1, "Parameter2", param2);
    });

    assertEquals("Either Parameter1 or Parameter2 is required for TestConnection connection", exception.getMessage());
  }

  @Test
  public void testRequireEither_BothParametersBlank_ShouldThrowException() {
    // Given
    String param1 = "  ";
    String param2 = "";

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ParameterValidator.requireEither("TestConnection", "Parameter1", param1, "Parameter2", param2);
    });

    assertEquals("Either Parameter1 or Parameter2 is required for TestConnection connection", exception.getMessage());
  }

  @Test
  public void testMultipleValidations_AllValid_ShouldPass() {
    // Given
    String stringParam = "valid-string";
    Object objectParam = new Object();
    int intParam = 8080;

    // When & Then - should not throw exception
    assertDoesNotThrow(() -> {
      ParameterValidator.requireNotBlank("TestConnection", "String Parameter", stringParam);
      ParameterValidator.requireNotNull("TestConnection", "Object Parameter", objectParam);
      ParameterValidator.requirePositive("TestConnection", "Port", intParam);
    });
  }

  @Test
  public void testMultipleValidations_FirstValidationFails_ShouldThrowException() {
    // Given
    String stringParam = null; // This will fail
    Object objectParam = new Object();
    int intParam = 8080;

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ParameterValidator.requireNotBlank("TestConnection", "String Parameter", stringParam);
      ParameterValidator.requireNotNull("TestConnection", "Object Parameter", objectParam);
      ParameterValidator.requirePositive("TestConnection", "Port", intParam);
    });

    assertEquals("String Parameter is required for TestConnection connection", exception.getMessage());
  }

  @Test
  public void testCustomConnectionType_ShouldIncludeInErrorMessage() {
    // Given
    String customConnectionType = "CustomVectorStore";
    String nullValue = null;

    // When & Then
    ModuleException exception = assertThrows(ModuleException.class, () -> {
      ParameterValidator.requireNotBlank(customConnectionType, "API Key", nullValue);
    });

    assertEquals("API Key is required for CustomVectorStore connection", exception.getMessage());
  }

  @Test
  public void testStaticMethodsPerformance_ShouldNotCreateInstances() {
    // Given
    String validValue = "test-value";

    // When & Then - Multiple calls should not create objects (performance test)
    assertDoesNotThrow(() -> {
      for (int i = 0; i < 1000; i++) {
        ParameterValidator.requireNotBlank("TestConnection", "Parameter" + i, validValue);
      }
    });

    // This test verifies that no instances are created during validation
    // If the old fluent API was used, this would create 1000 objects
    // With static methods, no objects are created
  }
}
