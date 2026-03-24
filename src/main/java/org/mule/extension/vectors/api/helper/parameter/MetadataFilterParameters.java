package org.mule.extension.vectors.api.helper.parameter;

import org.mule.extension.vectors.internal.helper.metadata.MetadataFilterHelper;

import java.util.Objects;

import dev.langchain4j.store.embedding.filter.Filter;

public abstract class MetadataFilterParameters {

  public abstract String getCondition();

  public boolean isConditionSet() {

    return getCondition() != null;
  }

  public Filter buildMetadataFilter() {

    if (!isConditionSet()) {

      throw new IllegalArgumentException("Filter condition is not set. Please provide a valid condition.");
    }

    Filter filter;

    try {

      filter = MetadataFilterHelper.fromExpression(getCondition());

    } catch (Exception e) {
      throw new IllegalArgumentException("Error building metadata filter: " + e.getMessage());
    }


    return filter;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    MetadataFilterParameters that = (MetadataFilterParameters) o;
    return Objects.equals(getCondition(), that.getCondition());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCondition());
  }
}
