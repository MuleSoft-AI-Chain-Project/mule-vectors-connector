package org.mule.extension.vectors.internal.metadata;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.json.api.JsonTypeLoader;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import org.mule.runtime.core.api.util.IOUtils;

import java.io.InputStream;
import java.util.Optional;

public class RowsOutputTypeMetadataResolver implements OutputTypeResolver<StoreConfiguration> {

  @Override
  public String getCategoryName() {
    return "row";
  }

  @Override
  public MetadataType getOutputType(MetadataContext metadataContext, StoreConfiguration storeConfiguration)
      throws MetadataResolvingException, ConnectionException {

    InputStream resourceAsStream = Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream("api/metadata/StoreQueryAllResponse.json");

    if (resourceAsStream == null) {
      return null;
    }

    Optional<MetadataType> metadataType = new JsonTypeLoader(IOUtils.toString(resourceAsStream))
        .load(null, "Load Store Response");

    return metadataType.orElse(null);
  }
}
