package org.mule.extension.vectors.internal.helper.media;

import static org.assertj.core.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

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

  @Test
  void process_fillStrategy_wideImage() throws IOException {
    BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    byte[] bytes = baos.toByteArray();

    ImageProcessor processor = ImageProcessor.builder()
        .targetWidth(50)
        .targetHeight(50)
        .compressionQuality(1.0f)
        .scaleStrategy(ImageProcessor.ScaleStrategy.FILL)
        .build();

    byte[] result = processor.process(bytes, "png");
    assertThat(result).isNotEmpty();
  }

  @Test
  void process_fillStrategy_tallImage() throws IOException {
    BufferedImage img = new BufferedImage(50, 100, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    byte[] bytes = baos.toByteArray();

    ImageProcessor processor = ImageProcessor.builder()
        .targetWidth(50)
        .targetHeight(50)
        .compressionQuality(1.0f)
        .scaleStrategy(ImageProcessor.ScaleStrategy.FILL)
        .build();

    byte[] result = processor.process(bytes, "png");
    assertThat(result).isNotEmpty();
  }

  @Test
  void process_stretchStrategy() throws IOException {
    BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    byte[] bytes = baos.toByteArray();

    ImageProcessor processor = ImageProcessor.builder()
        .targetWidth(50)
        .targetHeight(50)
        .compressionQuality(1.0f)
        .scaleStrategy(ImageProcessor.ScaleStrategy.STRETCH)
        .build();

    byte[] result = processor.process(bytes, "png");
    assertThat(result).isNotEmpty();
  }

  @Test
  void process_noCompression() throws IOException {
    BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    byte[] bytes = baos.toByteArray();

    ImageProcessor processor = ImageProcessor.builder()
        .targetWidth(50)
        .targetHeight(50)
        .compressionQuality(0f)
        .scaleStrategy(ImageProcessor.ScaleStrategy.FIT)
        .build();

    byte[] result = processor.process(bytes, "png");
    assertThat(result).isNotEmpty();
  }

  @Test
  void process_noResize() throws IOException {
    BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    byte[] bytes = baos.toByteArray();

    ImageProcessor processor = ImageProcessor.builder()
        .targetWidth(0)
        .targetHeight(0)
        .compressionQuality(0.8f)
        .scaleStrategy(ImageProcessor.ScaleStrategy.FIT)
        .build();

    byte[] result = processor.process(bytes, "png");
    assertThat(result).isNotEmpty();
  }

  @Test
  void process_autoFormatDetectsJpeg() throws IOException {
    BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "jpeg", baos);
    byte[] bytes = baos.toByteArray();

    ImageProcessor processor = ImageProcessor.builder()
        .targetWidth(25)
        .targetHeight(25)
        .compressionQuality(1.0f)
        .scaleStrategy(ImageProcessor.ScaleStrategy.FIT)
        .build();

    byte[] result = processor.process(bytes);
    assertThat(result).isNotEmpty();
  }

  @Test
  void builder_defaults() throws IOException {
    BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    byte[] bytes = baos.toByteArray();

    ImageProcessor processor = ImageProcessor.builder()
        .targetWidth(10)
        .targetHeight(10)
        .build();

    byte[] result = processor.process(bytes, "png");
    assertThat(result).isNotEmpty();
  }

  @Test
  void process_fitStrategy_largeImageDownscaled() throws IOException {
    BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    byte[] bytes = baos.toByteArray();

    ImageProcessor processor = ImageProcessor.builder()
        .targetWidth(50)
        .targetHeight(50)
        .compressionQuality(1.0f)
        .scaleStrategy(ImageProcessor.ScaleStrategy.FIT)
        .build();

    byte[] result = processor.process(bytes, "png");
    assertThat(result).isNotEmpty();
  }
}
