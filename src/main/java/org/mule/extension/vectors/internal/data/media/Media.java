package org.mule.extension.vectors.internal.data.media;

import java.util.Objects;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.video.Video;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;

/**
 * Represents a segment of multimodal data that can include text, image, video, and audio data, along with metadata.
 */
public class Media {

  private final String text;
  private final Image image;
  private final Video video;
  private final byte[] audioData;
  private final Metadata metadata;

  private Media(String text, Image image, Video video, byte[] audioData, Metadata metadata) {
    this.text = text;
    this.image = image;
    this.video = video;
    this.audioData = audioData;
    this.metadata = ValidationUtils.ensureNotNull(metadata, "metadata");
  }

  public String text() {
    return this.text;
  }

  public Image image() {
    return this.image;
  }

  public Video video() {
    return this.video;
  }

  public byte[] audioData() {
    return this.audioData;
  }

  public Metadata metadata() {
    return this.metadata;
  }

  public String metadata(String key) {
    return this.metadata.getString(key);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Media that = (Media) o;
    return Objects.equals(text, that.text) &&
        Objects.equals(image, that.image) &&
        Objects.equals(video, that.video) &&
        Objects.equals(metadata, that.metadata) &&
        Objects.equals(audioData, that.audioData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, image, video, audioData, metadata);
  }

  @Override
  public String toString() {
    return "MultimodalSegment { " +
        "text=" + Utils.quoted(text) +
        ", image=" + (image != null ? image.toString() : "null") +
        ", video=" + (video != null ? video.toString() : "null") +
        ", audioData=" + (audioData != null ? audioData.length + " bytes" : "null") +
        ", metadata=" + metadata.toMap() +
        " }";
  }

  // Factory method for text data
  public static Media fromText(String text, Metadata metadata) {
    return new Media(text, null, null, null, metadata);
  }

  // Factory method for image data
  public static Media fromImage(Image image, Metadata metadata) {
    return new Media(null, image, null, null, metadata);
  }

  // Factory method for image data
  public static Media fromImage(Image image) {
    return new Media(null, image, null, null, new Metadata());
  }

  // Factory method for video data
  public static Media fromVideo(Video video, Metadata metadata) {
    return new Media(null, null, video, null, metadata);
  }

  // Factory method for audio data
  public static Media fromAudio(byte[] audioData, Metadata metadata) {
    return new Media(null, null, null, audioData, metadata);
  }

  // Check if this segment contains text
  public boolean hasText() {
    return this.text != null;
  }

  // Check if this segment contains an image
  public boolean hasImage() {
    return this.image != null;
  }

  // Check if this segment contains video
  public boolean hasVideo() {
    return this.video != null;
  }

  // Check if this segment contains audio data
  public boolean hasAudioData() {
    return this.audioData != null;
  }
}
