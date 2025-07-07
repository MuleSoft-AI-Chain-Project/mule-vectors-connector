package org.mule.extension.vectors.internal.helper.document;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

class DocumentParserTest {

    @Test
    void parse_shouldReturnNullForNullInput() {
        DocumentParser parser = inputStream -> null;
        assertThat(parser.parse(null)).isNull();
    }

    @Test
    void parse_shouldReturnEmptyStringForEmptyInput() {
        DocumentParser parser = inputStream -> "";
        InputStream empty = new ByteArrayInputStream(new byte[0]);
        assertThat(parser.parse(empty)).isEmpty();
    }

    @Test
    void parse_shouldReturnStringForTypicalInput() {
        DocumentParser parser = inputStream -> "parsed-content";
        InputStream in = new ByteArrayInputStream("foo".getBytes());
        assertThat(parser.parse(in)).isEqualTo("parsed-content");
    }
} 