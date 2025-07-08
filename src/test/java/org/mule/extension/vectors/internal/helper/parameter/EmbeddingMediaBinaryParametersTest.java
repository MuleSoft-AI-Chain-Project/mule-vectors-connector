package org.mule.extension.vectors.internal.helper.parameter;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import static org.assertj.core.api.Assertions.*;

class EmbeddingMediaBinaryParametersTest {
    @Test
    void getMediaType_shouldDefaultToNull() {
        EmbeddingMediaBinaryParameters params = new EmbeddingMediaBinaryParameters();
        assertThat(params.getMediaType()).isNull();
    }

    @Test
    void getMediaType_shouldReflectSetValue() throws Exception {
        EmbeddingMediaBinaryParameters params = new EmbeddingMediaBinaryParameters();
        Field field = params.getClass().getDeclaredField("mediaType");
        field.setAccessible(true);
        field.set(params, "image/png");
        assertThat(params.getMediaType()).isEqualTo("image/png");
    }

    @Test
    void getLabel_and_getBinaryInputStream_shouldReflectSetValue() throws Exception {
        EmbeddingMediaBinaryParameters params = new EmbeddingMediaBinaryParameters();
        Field labelField = params.getClass().getDeclaredField("label");
        labelField.setAccessible(true);
        labelField.set(params, "test label");
        Field binaryField = params.getClass().getDeclaredField("binaryInputStream");
        binaryField.setAccessible(true);
        InputStream is = new ByteArrayInputStream(new byte[]{1,2,3});
        binaryField.set(params, is);
        assertThat(params.getLabel()).isEqualTo("test label");
        assertThat(params.getBinaryInputStream()).isSameAs(is);
    }

    @Test
    void toString_shouldContainMediaType() throws Exception {
        EmbeddingMediaBinaryParameters params = new EmbeddingMediaBinaryParameters();
        Field field = params.getClass().getDeclaredField("mediaType");
        field.setAccessible(true);
        field.set(params, "audio/wav");
        assertThat(params.toString()).contains("audio/wav");
    }
} 