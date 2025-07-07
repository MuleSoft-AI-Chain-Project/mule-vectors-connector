package org.mule.extension.vectors.internal.helper.media;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

class MediaProcessorTest {

    @Test
    void process_shouldReturnNullForNullInput() throws IOException {
        MediaProcessor processor = new MediaProcessor() {
            @Override
            public byte[] process(byte[] media, String format) {
                return null;
            }
            @Override
            public byte[] process(byte[] media) {
                return null;
            }
        };
        assertThat(processor.process((byte[]) null)).isNull();
        assertThat(processor.process(null, "png")).isNull();
    }

    @Test
    void process_shouldReturnEmptyArrayForEmptyInput() throws IOException {
        MediaProcessor processor = new MediaProcessor() {
            @Override
            public byte[] process(byte[] media, String format) {
                return new byte[0];
            }
            @Override
            public byte[] process(byte[] media) {
                return new byte[0];
            }
        };
        assertThat(processor.process(new byte[0])).isEmpty();
        assertThat(processor.process(new byte[0], "jpg")).isEmpty();
    }

    @Test
    void process_shouldReturnProcessedBytes() throws IOException {
        MediaProcessor processor = new MediaProcessor() {
            @Override
            public byte[] process(byte[] media, String format) {
                return (media != null && format != null) ? (format + new String(media)).getBytes() : null;
            }
            @Override
            public byte[] process(byte[] media) {
                return (media != null) ? ("raw" + new String(media)).getBytes() : null;
            }
        };
        byte[] input = "foo".getBytes();
        assertThat(new String(processor.process(input))).isEqualTo("rawfoo");
        assertThat(new String(processor.process(input, "png"))).isEqualTo("pngfoo");
    }
} 