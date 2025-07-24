package org.mule.extension.vectors.api.metadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Response attributes for document parsing operations.
 * <p>
 * This class encapsulates the metadata returned by document parsing operations,
 * including the name of the document parser used and any additional attributes
 * that may be provided by the parsing process.
 * </p>
 *
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ParserResponseAttributes implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String documentParserName;

  /**
   * Constructs a {@code ParserResponseAttributes} instance.
   * <p>
   * This constructor extracts the {@code documentParserName} field from the provided
   * attributes map and stores any remaining attributes in the {@code otherAttributes} map.
   * </p>
   *
   * @param requestAttributes a map containing the response attributes, where
   *                         {@code documentParserName} should be a String value
   */
  public ParserResponseAttributes(HashMap<String, Object> requestAttributes) {
    this.documentParserName = (String) requestAttributes.get("documentParserName");
  }

  /**
   * Gets the name of the document parser used for parsing.
   *
   * @return the document parser name, or null if not specified
   */
  public String getDocumentParserName() {
    return documentParserName;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ParserResponseAttributes that = (ParserResponseAttributes) o;
    return Objects.equals(documentParserName, that.documentParserName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(documentParserName);
  }

  @Override
  public String toString() {
    return "ParserResponseAttributes{" +
        "documentParserName='" + documentParserName + '\'' +
        '}';
  }
}
