package org.mule.extension.vectors.internal.helper.document;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;
import org.mule.runtime.extension.api.exception.ModuleException;

class TextDocumentParserTest {

    @Test
    void parse_shouldReturnTextContent() {
        TextDocumentParser parser = new TextDocumentParser();
        String text = "sample text";
        InputStream in = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        String result = parser.parse(in);
        assertThat(result).isEqualTo(text);
    }

    @Test
    void parse_shouldReturnEmptyStringForEmptyInput() {
        TextDocumentParser parser = new TextDocumentParser();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        assertThatThrownBy(() -> parser.parse(in))
            .isInstanceOf(ModuleException.class);
    }

    @Test
    void parse_shouldThrowForNullInputStream() {
        TextDocumentParser parser = new TextDocumentParser();
        Throwable thrown = catchThrowable(() -> parser.parse(null));
        assertThat(thrown)
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(NullPointerException.class);
    }
} 