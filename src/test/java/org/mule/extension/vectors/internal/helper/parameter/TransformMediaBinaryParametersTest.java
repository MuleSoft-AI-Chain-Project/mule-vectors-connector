package org.mule.extension.vectors.internal.helper.parameter;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import static org.assertj.core.api.Assertions.*;

class TransformMediaBinaryParametersTest {
    @Test
    void getMediaType_shouldDefaultToNull() {
        TransformMediaBinaryParameters params = new TransformMediaBinaryParameters();
        assertThat(params.getMediaType()).isNull();
    }

    @Test
    void getMediaType_shouldReflectSetValue() throws Exception {
        TransformMediaBinaryParameters params = new TransformMediaBinaryParameters();
        Field field = params.getClass().getDeclaredField("mediaType");
        field.setAccessible(true);
        field.set(params, "image/png");
        assertThat(params.getMediaType()).isEqualTo("image/png");
    }

    @Test
    void getBinaryInputStream_and_getMediaProcessorParameters_shouldReflectSetValue() throws Exception {
        TransformMediaBinaryParameters params = new TransformMediaBinaryParameters();
        Field binaryField = params.getClass().getDeclaredField("binaryInputStream");
        binaryField.setAccessible(true);
        InputStream is = new ByteArrayInputStream(new byte[]{1,2,3});
        binaryField.set(params, is);
        Field procField = params.getClass().getDeclaredField("mediaProcessorParameters");
        procField.setAccessible(true);
        MediaProcessorParameters mpp = new MediaProcessorParameters() {};
        procField.set(params, mpp);
        assertThat(params.getBinaryInputStream()).isSameAs(is);
        assertThat(params.getMediaProcessorParameters()).isSameAs(mpp);
    }

    @Test
    void toString_shouldContainMediaType() throws Exception {
        TransformMediaBinaryParameters params = new TransformMediaBinaryParameters();
        Field field = params.getClass().getDeclaredField("mediaType");
        field.setAccessible(true);
        field.set(params, "audio/wav");
        assertThat(params.toString()).contains("audio/wav");
    }
} 