package org.mule.extension.vectors.internal.data.media;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.video.Video;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MediaTest {

    @Test
    void fromText_andGetters() {
        Metadata metadata = new Metadata(Map.of("k", "v"));
        Media media = Media.fromText("hello", metadata);
        assertThat(media.text()).isEqualTo("hello");
        assertThat(media.image()).isNull();
        assertThat(media.video()).isNull();
        assertThat(media.audioData()).isNull();
        assertThat(media.metadata()).isEqualTo(metadata);
        assertThat(media.metadata("k")).isEqualTo("v");
        assertThat(media.hasText()).isTrue();
        assertThat(media.hasImage()).isFalse();
        assertThat(media.hasVideo()).isFalse();
        assertThat(media.hasAudioData()).isFalse();
    }

    @Test
    void fromImage_andGetters() {
        Image image = mock(Image.class);
        Metadata metadata = new Metadata();
        Media media = Media.fromImage(image, metadata);
        assertThat(media.text()).isNull();
        assertThat(media.image()).isEqualTo(image);
        assertThat(media.video()).isNull();
        assertThat(media.audioData()).isNull();
        assertThat(media.metadata()).isEqualTo(metadata);
        assertThat(media.hasText()).isFalse();
        assertThat(media.hasImage()).isTrue();
        assertThat(media.hasVideo()).isFalse();
        assertThat(media.hasAudioData()).isFalse();
    }

    @Test
    void fromImage_noMetadata() {
        Image image = mock(Image.class);
        Media media = Media.fromImage(image);
        assertThat(media.image()).isEqualTo(image);
        assertThat(media.metadata()).isNotNull();
    }

    @Test
    void fromVideo_andGetters() {
        Video video = mock(Video.class);
        Metadata metadata = new Metadata();
        Media media = Media.fromVideo(video, metadata);
        assertThat(media.text()).isNull();
        assertThat(media.image()).isNull();
        assertThat(media.video()).isEqualTo(video);
        assertThat(media.audioData()).isNull();
        assertThat(media.metadata()).isEqualTo(metadata);
        assertThat(media.hasVideo()).isTrue();
    }

    @Test
    void fromAudio_andGetters() {
        byte[] audio = new byte[] {1,2,3};
        Metadata metadata = new Metadata();
        Media media = Media.fromAudio(audio, metadata);
        assertThat(media.text()).isNull();
        assertThat(media.image()).isNull();
        assertThat(media.video()).isNull();
        assertThat(media.audioData()).isEqualTo(audio);
        assertThat(media.metadata()).isEqualTo(metadata);
        assertThat(media.hasAudioData()).isTrue();
    }

    @Test
    void equalsAndHashCode() {
        Metadata metadata = new Metadata(Map.of("k", "v"));
        Media m1 = Media.fromText("t", metadata);
        Media m2 = Media.fromText("t", metadata);
        Media m3 = Media.fromText("other", metadata);
        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        assertThat(m1).isNotEqualTo(m3);
        assertThat(m1).isNotEqualTo(null);
        assertThat(m1).isNotEqualTo("notMedia");
    }

    @Test
    void toString_includesFields() {
        Metadata metadata = new Metadata(Map.of("foo", "bar"));
        Media media = Media.fromText("abc", metadata);
        String str = media.toString();
        assertThat(str).contains("abc");
        assertThat(str).contains("foo");
        assertThat(str).contains("bar");
    }

    @Test
    void nullMetadata_throws() {
        assertThatThrownBy(() -> Media.fromText("t", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("metadata");
    }
} 