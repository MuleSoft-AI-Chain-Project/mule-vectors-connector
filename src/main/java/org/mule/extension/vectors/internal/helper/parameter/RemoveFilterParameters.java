package org.mule.extension.vectors.internal.helper.parameter;

import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.List;

@ExclusiveOptionals
public class RemoveFilterParameters extends MetadataFilterParameters {

  @Parameter
  @Alias("ids")
  @DisplayName("Ids")
  @Summary("List of ids to be removed")
  @Optional
  List<String> ids;

  @Parameter
  @Alias("condition")
  @DisplayName("Metadata Condition")
  @Placement(order = 2)
  @Example("file_name = 'example.pdf' AND (file_type = 'any' OR file_name = 'txt')")
  @Content
  @Optional
  String condition;

  public List<String> getIds() {
    return ids;
  }

  @Override
  public String getCondition() {
    return condition;
  }

  public void validate() {

    if(ids != null && !ids.isEmpty() && condition != null && condition.compareTo("") != 0) {
      throw new ModuleException(
          "Ids and Metadata condition are mutually exclusive",
          MuleVectorsErrorType.INVALID_PARAMETER);
    }
  }

  public boolean isIdsSet() {
    return ids != null && !ids.isEmpty();
  }
}
