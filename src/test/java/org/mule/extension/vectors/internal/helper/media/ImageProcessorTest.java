package org.mule.extension.vectors.internal.helper.media;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.*;

class ImageProcessorTest {

    @Test
    void process_shouldReturnBytesForValidImage() throws IOException {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] bytes = baos.toByteArray();
        ImageProcessor processor = ImageProcessor.builder()
                .targetWidth(2)
                .targetHeight(2)
                .compressionQuality(1.0f)
                .scaleStrategy(ImageProcessor.ScaleStrategy.FIT)
                .build();
        byte[] result = processor.process(bytes, "png");
        assertThat(result).isNotEmpty();
    }

    @Test
    void process_shouldReturnBytesForValidImageWithAutoFormat() throws IOException {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] bytes = baos.toByteArray();
        ImageProcessor processor = ImageProcessor.builder()
                .targetWidth(2)
                .targetHeight(2)
                .compressionQuality(1.0f)
                .scaleStrategy(ImageProcessor.ScaleStrategy.FIT)
                .build();
        byte[] result = processor.process(bytes);
        assertThat(result).isNotEmpty();
    }

    @Test
    void process_shouldThrowForNullBytes() {
        ImageProcessor processor = ImageProcessor.builder()
                .targetWidth(2)
                .targetHeight(2)
                .compressionQuality(1.0f)
                .scaleStrategy(ImageProcessor.ScaleStrategy.FIT)
                .build();
        assertThatThrownBy(() -> processor.process((byte[]) null, "png"))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> processor.process((byte[]) null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void process_shouldThrowForInvalidBytes() {
        ImageProcessor processor = ImageProcessor.builder()
                .targetWidth(2)
                .targetHeight(2)
                .compressionQuality(1.0f)
                .scaleStrategy(ImageProcessor.ScaleStrategy.FIT)
                .build();
        byte[] invalid = "notanimage".getBytes();
        assertThatThrownBy(() -> processor.process(invalid, "png"))
            .isInstanceOfAny(IOException.class, IllegalArgumentException.class);
        assertThatThrownBy(() -> processor.process(invalid))
            .isInstanceOfAny(IOException.class, IllegalArgumentException.class);
    }
} 