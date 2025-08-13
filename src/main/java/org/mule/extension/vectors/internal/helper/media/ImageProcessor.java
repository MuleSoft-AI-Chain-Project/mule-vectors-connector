package org.mule.extension.vectors.internal.helper.media;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageProcessor implements MediaProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageProcessor.class);

  public enum ScaleStrategy {
    FIT,
    FILL,
    STRETCH
  }

  private int targetWidth;
  private int targetHeight;
  // The compression quality (between 0.0 and 1.0, where 1.0 is highest quality)
  private float compressionQuality;
  private ScaleStrategy scaleStrategy;

  /**
   * Constructs an ImageProcessor with the specified target dimensions and compression quality.
   *
   * @param targetWidth        The desired width for processed images.
   * @param targetHeight       The desired height for processed images.
   * @param compressionQuality A float between 0.0 and 1.0 representing the quality of compression (1.0 being the highest).
   */
  public ImageProcessor(int targetWidth, int targetHeight, float compressionQuality, ScaleStrategy scaleStrategy) {
    this.targetWidth = targetWidth;
    this.targetHeight = targetHeight;
    this.compressionQuality = compressionQuality;
    this.scaleStrategy = scaleStrategy;
  }

  /**
   * Scales and pads the provided image to fit within the intended dimensions while maintaining its aspect ratio.
   *
   * <p>If the aspect ratio of the input image exceeds that of the target dimensions, the image is scaled
   * to match the target width. Otherwise, it is scaled to match the target height. Padding is applied as needed to satisfy the
   * exact target dimensions.
   *
   * @param image The {@link BufferedImage} to process.
   * @return A new {@link BufferedImage} resized and padded to the target dimensions.
   */
  private BufferedImage fit(BufferedImage image) {

    return Scalr.resize(image, Scalr.Method.QUALITY, targetWidth, targetHeight);
  }

  private BufferedImage fill(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();

    BufferedImage processedImage;

    // Compare aspect ratios as doubles to avoid integer division
    if ((double) width / targetWidth < (double) height / targetHeight) {
      // Fit to width
      processedImage = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, targetWidth);
    } else {
      // Fit to height
      processedImage = Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_HEIGHT, targetHeight);
    }

    // Calculate x and y positions for cropping
    int cropX = (processedImage.getWidth() - targetWidth) / 2;
    int cropY = (processedImage.getHeight() - targetHeight) / 2;

    // Crop the image to the target dimensions
    processedImage = Scalr.crop(processedImage, cropX, cropY, targetWidth, targetHeight);

    return processedImage;
  }

  /**
   * Resizes the given image to the specified target dimensions without adding padding.
   *
   * @param image The input {@link BufferedImage}.
   * @return A new {@link BufferedImage} resized directly to the target width and height.
   */
  private BufferedImage stretch(BufferedImage image) {
    return Scalr.resize(image, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, targetWidth, targetHeight);
  }

  /**
   * Adds black padding to the provided image to match the targeted width and height.
   *
   * @param image The input {@link BufferedImage} to be padded.
   * @return A new {@link BufferedImage} incorporating the original image along with added padding.
   */
  private BufferedImage pad(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();

    // Create a new image with padding
    BufferedImage paddedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = paddedImage.createGraphics();
    g.setColor(Color.BLACK); // Padding color
    g.fillRect(0, 0, targetWidth, targetHeight);

    // Center the image
    int x = (targetWidth - width) / 2;
    int y = (targetHeight - height) / 2;
    g.drawImage(image, x, y, null);
    g.dispose();

    return paddedImage;
  }

  /**
   * Compresses the input image using JPEG format, based on the defined compression quality.
   *
   * @param image The {@link BufferedImage} to compress.
   * @return A new {@link BufferedImage} representing the compressed image.
   * @throws Exception If an error occurs during compression.
   */
  private BufferedImage compress(BufferedImage image) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
      ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
      writer.setOutput(ios);

      ImageWriteParam param = writer.getDefaultWriteParam();
      param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
      param.setCompressionQuality(compressionQuality); // Set compression quality (0.0 to 1.0)

      writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
      writer.dispose();

      return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
    }
  }

  /**
   * Decodes a Base64-encoded string into a {@link BufferedImage}.
   *
   * @param base64 The Base64-encoded string representing the image data.
   * @return A decoded {@link BufferedImage}.
   * @throws Exception If an error occurs during decoding or image processing.
   */
  private BufferedImage base64ToBufferedImage(String base64) throws IOException {
    byte[] imageBytes = Base64.getDecoder().decode(base64);
    try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
      return ImageIO.read(bais);
    }
  }

  private BufferedImage bytesToBufferedImage(byte[] imageBytes) throws IOException {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
      return ImageIO.read(bais);
    }
  }

  private byte[] bufferedImageToBytes(BufferedImage image, String format) throws IOException {

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(image, format, baos); // Write the image in the specified format
      return baos.toByteArray();         // Get the byte array
    }
  }

  private BufferedImage process(BufferedImage image) throws IOException  {

    BufferedImage processedImage = image;

    if(targetHeight >0  && targetWidth > 0) {

      switch(scaleStrategy) {

        case FIT:
          processedImage = fit(processedImage);
          processedImage = pad(processedImage);
          break;

        case FILL:
          processedImage = fill(processedImage);
          break;

        case STRETCH:
          processedImage = stretch(processedImage);
          break;
      }
    }

    if(compressionQuality != 0f) {

      processedImage = compress(processedImage);
    }

    return processedImage;
  }

  @Override
  public byte[] process(byte[] mediaBytes, String format) throws IOException {

    return bufferedImageToBytes(process(bytesToBufferedImage(mediaBytes)), format);
  }

  @Override
  public byte[] process(byte[] mediaBytes) throws IOException {

    return process(mediaBytes, getImageFormat(mediaBytes));
  }

  /**
   * Determines the format of the provided image bytes.
   *
   * <p>This method reads the image data from the given byte array and attempts to detect its format
   * (e.g., "jpeg", "png", "webp"). If the format cannot be determined, it falls back to returning "png".
   *
   * @param imageBytes The byte array representing the image data.
   * @return A string representing the format of the image, such as "jpeg", "png", or "webp".
   */
  private String getImageFormat(byte[] imageBytes) {

    try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
        MemoryCacheImageInputStream input = new MemoryCacheImageInputStream(bais)) {

      Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
      if (readers.hasNext()) {
        ImageReader reader = readers.next();
        return reader.getFormatName(); // e.g., "jpeg", "png", "webp", etc.
      }
    } catch (Exception e) {
      LOGGER.warn("Unable to determine the image format");
    }
    return "png";
  }

  public static class Builder {

    /**
     * The target width to be applied during image processing.
     */
    private int targetWidth;

    /**
     * The target height to be applied during image processing.
     */
    private int targetHeight;


    private ScaleStrategy scaleStrategy = ImageProcessor.ScaleStrategy.FIT;

    /**
     * The level of compression quality desired for image processing.
     */
    private float compressionQuality;

    /**
     * Sets the target width for the image processor.
     *
     * @param targetWidth The desired target width.
     * @return The current {@link Builder} instance (fluent API).
     */
    public Builder targetWidth(int targetWidth) {
      this.targetWidth = targetWidth;
      return this;
    }

    /**
     * Sets the target height for the image processor.
     *
     * @param targetHeight The desired target height.
     * @return The current {@link Builder} instance (fluent API).
     */
    public Builder targetHeight(int targetHeight) {
      this.targetHeight = targetHeight;
      return this;
    }

    /**
     * Defines the image compression quality to be used by the processor.
     *
     * @param compressionQuality A float value between 0.0 and 1.0 indicating the desired quality.
     * @return The current {@link Builder} instance (fluent API).
     */
    public Builder compressionQuality(float compressionQuality) {
      this.compressionQuality = compressionQuality;
      return this;
    }

    public Builder scaleStrategy(ScaleStrategy scaleStrategy) {
      this.scaleStrategy = scaleStrategy;
      return this;
    }

    /**
     * Produces a fully-configured {@link ImageProcessor} instance based on the properties set in the builder.
     *
     * @return A newly instantiated {@link ImageProcessor}.
     */
    public ImageProcessor build() {
      return new ImageProcessor(targetWidth, targetHeight, compressionQuality, scaleStrategy);
    }
  }

  /**
   * Provides a new instance of the {@link Builder} for simplified configuration of an {@link ImageProcessor}.
   *
   * @return A fresh {@link Builder} instance.
   */
  public static Builder builder() {
    return new Builder();
  }
}
