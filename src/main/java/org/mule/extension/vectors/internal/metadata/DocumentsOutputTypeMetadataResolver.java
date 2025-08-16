package org.mule.extension.vectors.internal.metadata;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.json.api.JsonTypeLoader;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import org.mule.runtime.core.api.util.IOUtils;

import java.io.InputStream;
import java.util.Optional;

public class DocumentsOutputTypeMetadataResolver implements OutputTypeResolver<StorageConfiguration> {

  @Override
  public String getCategoryName() {
    return "document";
  }

  @Override
  public MetadataType getOutputType(MetadataContext metadataContext, StorageConfiguration storageConfiguration)
      throws MetadataResolvingException, ConnectionException {

    InputStream resourceAsStream = Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream("api/metadata/StorageLoadListResponse.json");

    Optional<MetadataType> metadataType = new JsonTypeLoader(IOUtils.toString(resourceAsStream))
        .load(null, "Load Documents Response");

    return metadataType.orElse(null);
  }
}
