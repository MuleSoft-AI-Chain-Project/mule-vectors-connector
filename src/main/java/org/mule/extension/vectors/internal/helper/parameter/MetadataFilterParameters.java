package org.mule.extension.vectors.internal.helper.parameter;

import dev.langchain4j.store.embedding.filter.Filter;
import org.mule.extension.vectors.internal.helper.metadata.MetadataFilterHelper;

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
}
