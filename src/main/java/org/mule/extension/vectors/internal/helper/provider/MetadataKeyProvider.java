package org.mule.extension.vectors.internal.helper.provider;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;
import org.mule.runtime.extension.api.values.ValueResolvingException;

import java.util.Set;

public class MetadataKeyProvider implements ValueProvider {

  @Override
  public Set<Value> resolve() throws ValueResolvingException {

    return ValueBuilder.getValuesFor(
                                     Constants.METADATA_KEY_SOURCE_ID,
                                     Constants.METADATA_KEY_FILE_NAME,
                                     Constants.METADATA_KEY_FILE_TYPE,
                                     Constants.METADATA_KEY_URL,
                                     Constants.METADATA_KEY_ABSOLUTE_DIRECTORY_PATH,
                                     Constants.METADATA_KEY_SOURCE,
                                     Constants.METADATA_KEY_INGESTION_DATETIME,
                                     Constants.METADATA_KEY_INGESTION_TIMESTAMP);// "textSegment"
  }

}
