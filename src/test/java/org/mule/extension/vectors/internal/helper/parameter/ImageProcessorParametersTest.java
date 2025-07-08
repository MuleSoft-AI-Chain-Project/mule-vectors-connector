package org.mule.extension.vectors.internal.helper.parameter;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.helper.media.ImageProcessor;
import java.lang.reflect.Field;
import static org.assertj.core.api.Assertions.*;

class ImageProcessorParametersTest {
    @Test
    void getTargetWidth_shouldDefaultToZero() {
        ImageProcessorParameters params = new ImageProcessorParameters();
        // Java default is 0; Mule sets @Optional(defaultValue) at runtime
        assertThat(params.getTargetWidth()).isZero();
    }

    @Test
    void getTargetWidth_shouldReflectSetValue() throws Exception {
        ImageProcessorParameters params = new ImageProcessorParameters();
        Field field = params.getClass().getDeclaredField("targetWidth");
        field.setAccessible(true);
        field.set(params, 123);
        assertThat(params.getTargetWidth()).isEqualTo(123);
    }

    @Test
    void getTargetHeight_shouldDefaultToZero() {
        ImageProcessorParameters params = new ImageProcessorParameters();
        // Java default is 0; Mule sets @Optional(defaultValue) at runtime
        assertThat(params.getTargetHeight()).isZero();
    }

    @Test
    void getTargetHeight_shouldReflectSetValue() throws Exception {
        ImageProcessorParameters params = new ImageProcessorParameters();
        Field field = params.getClass().getDeclaredField("targetHeight");
        field.setAccessible(true);
        field.set(params, 321);
        assertThat(params.getTargetHeight()).isEqualTo(321);
    }

    @Test
    void getCompressionQuality_shouldDefaultToZero() {
        ImageProcessorParameters params = new ImageProcessorParameters();
        // Java default is 0.0f; Mule sets @Optional(defaultValue) at runtime
        assertThat(params.getCompressionQuality()).isZero();
    }

    @Test
    void getCompressionQuality_shouldReflectSetValue() throws Exception {
        ImageProcessorParameters params = new ImageProcessorParameters();
        Field field = params.getClass().getDeclaredField("compressionQuality");
        field.setAccessible(true);
        field.set(params, 0.5f);
        assertThat(params.getCompressionQuality()).isEqualTo(0.5f);
    }

    @Test
    void getScaleStrategy_shouldDefaultToNull() {
        ImageProcessorParameters params = new ImageProcessorParameters();
        // Java default is null; Mule sets @Optional(defaultValue) at runtime
        assertThat(params.getScaleStrategy()).isNull();
    }

    @Test
    void getScaleStrategy_shouldReflectSetValue() throws Exception {
        ImageProcessorParameters params = new ImageProcessorParameters();
        Field field = params.getClass().getDeclaredField("scaleStrategy");
        field.setAccessible(true);
        field.set(params, ImageProcessor.ScaleStrategy.FILL);
        assertThat(params.getScaleStrategy()).isEqualTo(ImageProcessor.ScaleStrategy.FILL);
    }

    @Test
    void toString_shouldContainAllFields() throws Exception {
        ImageProcessorParameters params = new ImageProcessorParameters();
        Field widthField = params.getClass().getDeclaredField("targetWidth");
        widthField.setAccessible(true);
        widthField.set(params, 100);
        Field heightField = params.getClass().getDeclaredField("targetHeight");
        heightField.setAccessible(true);
        heightField.set(params, 200);
        Field qualityField = params.getClass().getDeclaredField("compressionQuality");
        qualityField.setAccessible(true);
        qualityField.set(params, 0.8f);
        Field scaleField = params.getClass().getDeclaredField("scaleStrategy");
        scaleField.setAccessible(true);
        scaleField.set(params, ImageProcessor.ScaleStrategy.FILL);
        String str = params.toString();
        assertThat(str).contains("100").contains("200").contains("0.8").contains("FILL");
    }
} 