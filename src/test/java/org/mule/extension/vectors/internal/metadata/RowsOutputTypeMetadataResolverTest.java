package org.mule.extension.vectors.internal.metadata;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RowsOutputTypeMetadataResolverTest {

  @Mock
  MetadataContext metadataContext;
  @Mock
  StoreConfiguration storeConfiguration;

  RowsOutputTypeMetadataResolver resolver = new RowsOutputTypeMetadataResolver();

  @Test
  void getCategoryName_returnsRow() {
    assertThat(resolver.getCategoryName()).isEqualTo("row");
  }

  @Test
  void getOutputType_normalFlow() throws Exception {
    // Assumes src/test/resources/api/metadata/StoreQueryAllResponse.json exists
    MetadataType type = resolver.getOutputType(metadataContext, storeConfiguration);
    assertThat(type).isNotNull();
  }
}
