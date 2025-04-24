package org.mule.extension.vectors.internal.helper.provider;

import java.util.Set;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;
import org.mule.runtime.extension.api.values.ValueResolvingException;

public class PayloadContentTypeProvider implements ValueProvider {

  @Override
  public Set<Value> resolve() throws ValueResolvingException {

    return ValueBuilder.getValuesFor(
            Constants.PAYLOAD_CONTENT_TYPE_BINARY,
            Constants.PAYLOAD_CONTENT_TYPE_BASE64);
  }

}
