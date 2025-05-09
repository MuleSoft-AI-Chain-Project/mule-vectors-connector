package org.mule.extension.vectors.internal.helper.metadata;

import dev.langchain4j.store.embedding.filter.*;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.*;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

public class MetadataFilterHelper {

  protected static final Logger LOGGER = LoggerFactory.getLogger(MetadataFilterHelper.class);

  private static final Pattern CONDITION_PATTERN =
      Pattern.compile("(?:([a-zA-Z_][a-zA-Z0-9_]*)\\s*([=!><]+)\\s*(.+)|CONTAINS\\s*\\(\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*,\\s*(.+?)\\s*\\))");

  private static final Pattern NUMBER_PATTERN =
      Pattern.compile("^-?\\d+(\\.\\d+)?$");

  private enum OperatorType {
    AND, OR, NONE
  }

  public static Filter fromExpression(String expression) {
    if (expression == null || expression.trim().isEmpty()) {
      throw new IllegalArgumentException("Expression cannot be null or empty");
    }

    expression = expression.trim();
    LOGGER.debug("Processing expression: " + expression);

    while (expression.startsWith("(") && expression.endsWith(")") &&
        isRedundantlyWrapped(expression)) {
      LOGGER.debug("Stripping redundant outer parentheses from: " + expression);
      expression = expression.substring(1, expression.length() - 1).trim();
      LOGGER.debug("Expression after stripping: " + expression);
    }

    List<String> tokens = splitExpression(expression);
    LOGGER.debug("splitExpression tokens for '" + expression + "': " + tokens);

    if (tokens.size() > 1) {
      OperatorType opType = getOperatorTypeForCurrentLevel(expression);
      boolean currentLevelIsAnd;

      if (opType == OperatorType.AND) {
        currentLevelIsAnd = true;
      } else if (opType == OperatorType.OR) {
        currentLevelIsAnd = false;
      } else {
        throw new IllegalArgumentException("Could not determine consistent logical operator for expression: " + expression + ". Tokens found: " + tokens.size());
      }

      List<Filter> subFilters = new ArrayList<>();
      for (String token : tokens) {
        if (token.trim().isEmpty()) {
          throw new IllegalArgumentException("Empty condition in expression: " + expression);
        }
        subFilters.add(fromExpression(token.trim()));
      }
      return createCompositeFilter(subFilters, currentLevelIsAnd);
    }

    if (expression.startsWith("(") && expression.endsWith(")") && isRedundantlyWrapped(expression) ) {
      LOGGER.debug("Processing single token as parenthesized sub-expression: " + expression);
      return fromExpression(expression.substring(1, expression.length() - 1).trim());
    }

    // Parse simple condition
    Matcher matcher = CONDITION_PATTERN.matcher(expression);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid condition format: " + expression);
    }

    String field, operator, valueStr;
    
    // Check if this is a function call (CONTAINS)
    if (matcher.group(4) != null) {
      field = matcher.group(4);
      operator = "CONTAINS";
      valueStr = matcher.group(5).trim();
    } else {
      field = matcher.group(1);
      operator = matcher.group(2);
      valueStr = matcher.group(3).trim();
    }

    // Handle quoted strings
    if ((valueStr.startsWith("'") && valueStr.endsWith("'")) ||
        (valueStr.startsWith("\"") && valueStr.endsWith("\""))) {
      valueStr = valueStr.substring(1, valueStr.length() - 1);
    }

    // Parse value
    Object value = parseValue(valueStr);

    return createFilter(field, operator, value);
  }

  private static boolean isRedundantlyWrapped(String expression) {
    if (!expression.startsWith("(") || !expression.endsWith(")")) {
      return false;
    }
    int balance = 0;
    for (int i = 0; i < expression.length() -1; i++) {
      if (expression.charAt(i) == '(') {
        balance++;
      } else if (expression.charAt(i) == ')') {
        balance--;
      }
      if (balance == 0 && i < expression.length() - 1) {
        return false;
      }
    }
    return balance == 1;
  }

  private static OperatorType getOperatorTypeForCurrentLevel(String expression) {
    int balance = 0;
    for (int i = 0; i < expression.length(); i++) {
      char c = expression.charAt(i);
      if (c == '(') {
        balance++;
      } else if (c == ')') {
        balance--;
        if (balance < 0) {
          throw new IllegalArgumentException("Mismatched parentheses leading to negative balance at index " + i + " in: " + expression);
        }
      } else if (balance == 0) {
        if (checkLogicalOperator(expression, i, "AND")) {
          return OperatorType.AND;
        }
        if (checkLogicalOperator(expression, i, "OR")) {
          return OperatorType.OR;
        }
      }
    }
    return OperatorType.NONE;
  }

  private static Object parseValue(String value) {
    if (NUMBER_PATTERN.matcher(value).matches()) {
      try {
        if (value.contains(".")) {
          return Double.parseDouble(value);
        } else {
          return Long.parseLong(value);
        }
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid number format for value '" + value + "' that matched number pattern.");
      }
    }
    return value;
  }

  private static Filter createFilter(String field, String operator, Object value) {

    String methodName ="";
    try {
      MetadataFilterBuilder filterBuilder = metadataKey(field);
      
      // Handle CONTAINS operator specially
      if ("CONTAINS".equals(operator)) {
        String stringValue = value.toString();
        // Remove quotes if present
        if ((stringValue.startsWith("'") && stringValue.endsWith("'")) || 
            (stringValue.startsWith("\"") && stringValue.endsWith("\""))) {
          stringValue = stringValue.substring(1, stringValue.length() - 1);
        }
        return filterBuilder.containsString(stringValue);
      }

      methodName = getFilterMethod(operator);
      if (value == null) {
        throw new IllegalArgumentException("Value cannot be null for operator " + operator + " on field " + field);
      }
      Method method = filterBuilder.getClass().getMethod(methodName, Utils.getPrimitiveTypeClass(value)); // Line 187 from stack trace
      return (Filter) method.invoke(filterBuilder, value);

    } catch (NoSuchMethodException nsme) {
      String typeName = (value != null) ? value.getClass().getName() : "null";
      // Provide more info about the expected parameter type based on Utils.getPrimitiveTypeClass(value)
      Class<?> expectedParamType = (value != null) ? Utils.getPrimitiveTypeClass(value) : null;
      String expectedParamTypeName = (expectedParamType != null) ? expectedParamType.getName() : "unknown (due to null value)";

      LOGGER.error("No such method '" + methodName + "' for value type '" + typeName + "' (resolved to parameter type '" + expectedParamTypeName + "'). Field: " + field + ", Value: " + value, nsme);
      throw new IllegalArgumentException("Failed to create filter. Method '" + methodName + "' with parameter type '" + expectedParamTypeName + "' not found. Details: " + nsme.getMessage());
    }
    catch (Exception e) {
      LOGGER.error("Error creating filter: " + e.getMessage(), e);
      throw new IllegalArgumentException("Failed to create filter: " + e.getMessage());
    }
  }

  private static String getFilterMethod(String operator) {
    switch (operator) {
      case "=":
        return Constants.METADATA_FILTER_METHOD_IS_EQUAL_TO;
      case "!=":
        return Constants.METADATA_FILTER_METHOD_IS_NOT_EQUAL_TO;
      case ">":
        return Constants.METADATA_FILTER_METHOD_IS_GREATER_THAN;
      case ">=":
        return Constants.METADATA_FILTER_METHOD_IS_GREATER_THAN_OR_EQUAL_TO;
      case "<":
        return Constants.METADATA_FILTER_METHOD_IS_LESS_THAN;
      case "<=":
        return Constants.METADATA_FILTER_METHOD_IS_LESS_THAN_OR_EQUAL_TO;
      default:
        throw new IllegalArgumentException("Unsupported operator: " + operator);
    }
  }

  private static List<String> splitExpression(String expression) {
    List<String> parts = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    int openParens = 0;
    boolean firstOperatorIsAnd = false;
    boolean firstOperatorIsOr = false;
    boolean firstOperatorFound = false;

    for (int i = 0; i < expression.length(); i++) {
      char c = expression.charAt(i);

      if (c == '(') {
        openParens++;
      } else if (c == ')') {
        openParens--;
        if (openParens < 0) {
          throw new IllegalArgumentException("Mismatched parentheses in expression (extra closing): " + expression);
        }
      }

      // Check for operators only if not inside parentheses
      if (openParens == 0 && (Character.isUpperCase(c) || Character.isLetter(c))) { // Potential start of AND or OR
        boolean isAndOp = checkLogicalOperator(expression, i, "AND");
        boolean isOrOp = checkLogicalOperator(expression, i, "OR");

        if (isAndOp || isOrOp) {
          if (!firstOperatorFound) {
            firstOperatorIsAnd = isAndOp;
            firstOperatorIsOr = isOrOp;
            firstOperatorFound = true;
          } else {
            if ((firstOperatorIsAnd && isOrOp) || (firstOperatorIsOr && isAndOp)) {
              throw new IllegalArgumentException("Mixed AND/OR operations at the same level must be explicitly grouped with parentheses. Expression: " + expression);
            }
          }

          if (current.length() > 0) {
            parts.add(current.toString().trim());
            current.setLength(0);
          }
          i += (isAndOp ? "AND".length() : "OR".length()) - 1;
          continue;
        }
      }
      current.append(c);
    }

    if (openParens != 0) {
      throw new IllegalArgumentException("Mismatched parentheses in expression (extra opening): " + expression);
    }

    if (current.length() > 0) {
      parts.add(current.toString().trim());
    }

    if (parts.isEmpty() && !expression.trim().isEmpty()) {
      parts.add(expression.trim()); // Handles case where expression is a single token without operators
    }
    return parts;
  }

  private static boolean checkLogicalOperator(String expression, int index, String operator) {
    if (index + operator.length() > expression.length()) { // Boundary check
      return false;
    }
    // Case-insensitive check for the operator itself
    if (!expression.substring(index, index + operator.length()).equalsIgnoreCase(operator)) {
      return false;
    }
    // Check char before operator (if not start of string)
    if (index > 0 && !Character.isWhitespace(expression.charAt(index - 1))) {
      return false;
    }
    // Check char after operator (if not end of string)
    if ((index + operator.length()) < expression.length() &&
        !Character.isWhitespace(expression.charAt(index + operator.length()))) {
      return false;
    }
    return true;
  }

  private static Filter createCompositeFilter(List<Filter> subFilters, boolean isAnd) {
    if (subFilters.isEmpty()) {
      throw new IllegalArgumentException("Cannot create composite filter with no subfilters");
    }
    if (subFilters.size() == 1) {
      return subFilters.get(0);
    }

    Filter result = subFilters.get(0);
    for (int i = 1; i < subFilters.size(); i++) {
      if (isAnd) {
        result = result.and(subFilters.get(i));
      } else {
        result = result.or(subFilters.get(i));
      }
    }
    return result;
  }
}
