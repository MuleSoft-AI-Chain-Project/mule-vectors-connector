package org.mule.extension.vectors.internal.helper.parameter;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.helper.document.DocumentParser;
import org.mule.extension.vectors.internal.helper.document.MultiformatDocumentParser;

import static org.assertj.core.api.Assertions.*;

class MultiformatDocumentParserParametersTest {

    @Test
    void getDocumentParser_shouldReturnMultiformatParser() {
        MultiformatDocumentParserParameters params = new MultiformatDocumentParserParameters();
        DocumentParser parser = params.getDocumentParser();
        assertThat(parser).isInstanceOf(MultiformatDocumentParser.class);
    }

    @Test
    void isIncludeMetadata_shouldDefaultToFalse() {
        MultiformatDocumentParserParameters params = new MultiformatDocumentParserParameters();
        assertThat(params.isIncludeMetadata()).isFalse();
    }

    @Test
    void isIncludeMetadata_shouldReflectSetValue() throws Exception {
        MultiformatDocumentParserParameters params = new MultiformatDocumentParserParameters();
        java.lang.reflect.Field field = params.getClass().getDeclaredField("includeMetadata");
        field.setAccessible(true);
        field.set(params, true);
        assertThat(params.isIncludeMetadata()).isTrue();
    }
} 