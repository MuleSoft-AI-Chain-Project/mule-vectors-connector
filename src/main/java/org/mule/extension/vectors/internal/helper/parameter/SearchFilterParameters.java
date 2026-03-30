package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.extension.vectors.api.helper.parameter.MetadataFilterParameters;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;

import java.util.Objects;

public class SearchFilterParameters extends MetadataFilterParameters {

  @Parameter
  @Alias("condition")
  @DisplayName("Metadata Condition")
  @Example("file_name = 'example.pdf' AND (file_type = 'any' OR file_name = 'txt')")
  @Optional
  @Content
  String condition;

  @Override
  public String getCondition() {
    return condition;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SearchFilterParameters that = (SearchFilterParameters) o;
    return Objects.equals(condition, that.condition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(condition);
  }
}
