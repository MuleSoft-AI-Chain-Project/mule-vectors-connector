package org.mule.extension.vectors.internal.helper.parameter;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.helper.document.DocumentParser;
import static org.assertj.core.api.Assertions.*;

class DocumentParserParametersTest {
    @Test
    void getDocumentParser_returnsExpected() {
        DocumentParser expected = new DocumentParser() {
            @Override
            public String parse(java.io.InputStream is) { return "dummy"; }
        };
        DocumentParserParameters params = () -> expected;
        assertThat(params.getDocumentParser()).isSameAs(expected);
    }
} 