package org.mule.extension.vectors.internal.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mule.extension.vectors.internal.config.StoreConfiguration;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RowsOutputTypeMetadataResolverTest {
    @Mock MetadataContext metadataContext;
    @Mock StoreConfiguration storeConfiguration;

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