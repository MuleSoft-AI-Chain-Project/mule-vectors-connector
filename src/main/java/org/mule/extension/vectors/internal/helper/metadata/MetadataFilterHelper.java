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

  public static Filter fromExpression(String expression) {
    if (expression == null || expression.trim().isEmpty()) {
      throw new IllegalArgumentException("Expression cannot be null or empty");
    }

    expression = expression.trim();
    LOGGER.debug("Processing expression: " + expression);

    // Handle parentheses (recursive case)
    if (expression.startsWith("(") && expression.endsWith(")")) {
      // Verify matching parentheses
      if (countParentheses(expression) != 0) {
        throw new IllegalArgumentException("Mismatched parentheses in expression: " + expression);
      }
      return fromExpression(expression.substring(1, expression.length() - 1).trim());
    }

    // Split by "AND" or "OR" while respecting parentheses
    List<String> tokens = splitExpression(expression);
    LOGGER.debug("splitExpression tokens: " + tokens);

    if (tokens.size() > 1) {

      boolean isAnd = (expression.toUpperCase().contains("AND") && expression.toUpperCase().indexOf("AND") < expression.toUpperCase().indexOf("OR")) ||
          !expression.toUpperCase().contains("OR");

      List<Filter> subFilters = new ArrayList<>();
      for (String token : tokens) {
        if (token.trim().isEmpty()) {
          throw new IllegalArgumentException("Empty condition in expression: " + expression);
        }
        subFilters.add(fromExpression(token.trim()));
      }
      return createCompositeFilter(subFilters, isAnd);
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
    if (valueStr.startsWith("'") && valueStr.endsWith("'") ||
        valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
      valueStr = valueStr.substring(1, valueStr.length() - 1);
    }

    // Parse value
    Object value = parseValue(valueStr);

    return createFilter(field, operator, value);
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
        throw new IllegalArgumentException("Invalid number format: " + value);
      }
    }
    return value;
  }

  private static Filter createFilter(String field, String operator, Object value) {
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

      String methodName = getFilterMethod(operator);
      Method method = filterBuilder.getClass().getMethod(methodName, Utils.getPrimitiveTypeClass(value));
      return (Filter) method.invoke(filterBuilder, value);

    } catch (Exception e) {
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
    boolean hasAnd = false;
    boolean hasOr = false;

    for (int i = 0; i < expression.length(); i++) {
      char c = expression.charAt(i);

      if (c == '(') openParens++;
      else if (c == ')') openParens--;

      // Check for logical operators
      if (openParens == 0) {
        hasAnd = hasAnd || checkLogicalOperator(expression, i, "AND");
        hasOr = hasOr || checkLogicalOperator(expression, i, "OR");
        if(hasAnd && hasOr) {
          throw new IllegalArgumentException("Mixed AND/OR operations must be explicitly grouped with parentheses. Expression: " + expression);
        }
        if (checkLogicalOperator(expression, i, "AND") ||
            checkLogicalOperator(expression, i, "OR")) {
          if (current.length() > 0) {
            parts.add(current.toString().trim());
            current.setLength(0);
          }
          // Skip the operator
          i += expression.substring(i).toUpperCase().startsWith("AND") ? 2 : 1;
          continue;
        }
      }

      current.append(c);
    }

    if (current.length() > 0) {
      parts.add(current.toString().trim());
    }

    return parts;
  }

  private static boolean checkLogicalOperator(String expression, int index, String operator) {
    String upperExpression = expression.toUpperCase();
    return upperExpression.startsWith(operator, index) &&
        (index == 0 || Character.isWhitespace(expression.charAt(index - 1))) &&
        (index + operator.length() >= expression.length() ||
            Character.isWhitespace(expression.charAt(index + operator.length())));
  }

  private static int countParentheses(String expression) {
    int count = 0;
    for (char c : expression.toCharArray()) {
      if (c == '(') count++;
      else if (c == ')') count--;
      if (count < 0) return count; // Closing parenthesis without matching opening
    }
    return count;
  }

  private static Filter createCompositeFilter(List<Filter> subFilters, boolean isAnd) {
    if (subFilters.isEmpty()) {
      throw new IllegalArgumentException("Cannot create composite filter with no subfilters");
    }
    if (subFilters.size() == 1) {
      return subFilters.get(0);
    }
    return isAnd ?
        subFilters.get(0).and(createCompositeFilter(subFilters.subList(1, subFilters.size()), isAnd)) :
        subFilters.get(0).or(createCompositeFilter(subFilters.subList(1, subFilters.size()), isAnd));
  }
}
