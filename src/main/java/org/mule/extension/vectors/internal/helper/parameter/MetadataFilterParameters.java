package org.mule.extension.vectors.internal.helper.parameter;

import dev.langchain4j.store.embedding.filter.Filter;
import org.mule.extension.vectors.internal.helper.metadata.MetadataFilterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MetadataFilterParameters {

  protected static final Logger LOGGER = LoggerFactory.getLogger(MetadataFilterParameters.class);

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

      LOGGER.error("Error building metadata filter: {}", e.getMessage());
      throw new IllegalArgumentException("Error building metadata filter: " + e.getMessage());
    }


    return filter;
  }
}
