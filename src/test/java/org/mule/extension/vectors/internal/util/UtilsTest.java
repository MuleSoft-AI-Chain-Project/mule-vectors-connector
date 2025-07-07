package org.mule.extension.vectors.internal.util;

import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.constant.Constants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UtilsTest {

    @Test
    void getCurrentISO8601Timestamp_shouldReturnValidFormat() {
        String timestamp = Utils.getCurrentISO8601Timestamp();
        assertThat(timestamp).matches("\\d{4}-\\d{2}-\\d{2}T.*Z");
    }

    @Test
    void getCurrentTimeMillis_shouldBeCloseToSystem() {
        long now = System.currentTimeMillis();
        long utilNow = Utils.getCurrentTimeMillis();
        assertThat(utilNow).isCloseTo(now, within(1000L));
    }

    @Test
    void getFileNameFromPath_shouldExtractFileName() {
        assertThat(Utils.getFileNameFromPath("/foo/bar/baz.txt")).isEqualTo("baz.txt");
        assertThat(Utils.getFileNameFromPath("baz.txt")).isEqualTo("baz.txt");
        assertThat(Utils.getFileNameFromPath("/foo/bar/.hidden")).isEqualTo(".hidden");
    }

    @Test
    void getPrimitiveTypeClass_shouldReturnPrimitiveOrClass() {
        assertThat(Utils.getPrimitiveTypeClass(5)).isEqualTo(int.class);
        assertThat(Utils.getPrimitiveTypeClass(5L)).isEqualTo(long.class);
        assertThat(Utils.getPrimitiveTypeClass(5.0)).isEqualTo(double.class);
        assertThat(Utils.getPrimitiveTypeClass("foo")).isEqualTo(String.class);
    }

    @Test
    void convertStringToType_shouldConvertCorrectly() {
        UUID uuid = UUID.randomUUID();
        assertThat(Utils.convertStringToType(uuid.toString())).isEqualTo(uuid);
        assertThat(Utils.convertStringToType("42")).isEqualTo(42);
        assertThat(Utils.convertStringToType("42.5")).isEqualTo(42.5);
        assertThat(Utils.convertStringToType("1234567890123")).isEqualTo(1234567890123L);
        assertThat(Utils.convertStringToType("notanumber")).isEqualTo("notanumber");
    }

    @Test
    void getFileExtension_shouldReturnExtensionOrEmpty() {
        assertThat(Utils.getFileExtension("foo.txt")).isEqualTo("txt");
        assertThat(Utils.getFileExtension("foo.bar.baz.pdf")).isEqualTo("pdf");
        assertThat(Utils.getFileExtension("foo"))
            .isEmpty();
        assertThat(Utils.getFileExtension(".hidden"))
            .isEmpty();
    }

    @Test
    void getMimeTypeFallback_shouldReturnMimeTypeOrFallback() {
        assertThat(Utils.getMimeTypeFallback(Paths.get("foo.jpg"))).isEqualTo("image/jpeg");
        assertThat(Utils.getMimeTypeFallback(Paths.get("foo.png"))).isEqualTo("image/png");
        assertThat(Utils.getMimeTypeFallback(Paths.get("foo.unknown"))).isEqualTo("application/octet-stream");
    }

    @Test
    void quoted_shouldReturnQuotedStringOrNull() {
        assertThat(Utils.quoted("foo")).isEqualTo("\"foo\"");
        assertThat(Utils.quoted(null)).isEqualTo("null");
        assertThat(Utils.quoted(42)).isEqualTo("\"42\"");
    }

    @Test
    void getOrDefault_shouldReturnValueOrDefault() {
        assertThat(Utils.getOrDefault("foo", "bar")).isEqualTo("foo");
        assertThat(Utils.getOrDefault(null, "bar")).isEqualTo("bar");
    }

    @Test
    void splitTextIntoTextSegments_shouldSplitOrReturnSingle() {
        String text = "abcdefghij";
        List<TextSegment> segments = Utils.splitTextIntoTextSegments(text, 3, 1);
        assertThat(segments).isNotEmpty();
        assertThat(segments.get(0).text()).contains("a");
        // No split
        List<TextSegment> single = Utils.splitTextIntoTextSegments(text, 0, 0);
        assertThat(single).hasSize(1);
        assertThat(single.get(0).text()).isEqualTo(text);
    }

    @Test
    void getDocumentParser_shouldReturnCorrectParserOrThrow() {
        assertThat(Utils.getDocumentParser(Constants.FILE_PARSER_TYPE_TEXT)).isInstanceOf(TextDocumentParser.class);
        assertThat(Utils.getDocumentParser(Constants.FILE_PARSER_TYPE_APACHE_TIKA)).isInstanceOf(ApacheTikaDocumentParser.class);
        assertThatThrownBy(() -> Utils.getDocumentParser("badtype")).isInstanceOf(IllegalArgumentException.class);
    }
} 