package org.mule.extension.vectors.internal.helper.parameter;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.api.parameter.MultiformatDocumentParserParameters;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.helper.document.DocumentParser;
import org.mule.extension.vectors.internal.helper.document.MultiformatDocumentParser;

import org.junit.jupiter.api.Test;

class MultiformatDocumentParserParametersTest {

  @Test
  void getDocumentParser_shouldReturnMultiformatParser() {
    MultiformatDocumentParserParameters params = new MultiformatDocumentParserParameters();
    DocumentParser parser = params.getDocumentParser();
    assertThat(parser).isInstanceOf(MultiformatDocumentParser.class);
  }

  @Test
  void getName_shouldReturnMultiformatConstant() {
    MultiformatDocumentParserParameters params = new MultiformatDocumentParserParameters();
    assertThat(params.getName()).isEqualTo(Constants.TRANSORMER_PARSER_MULTIFORMAT);
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

  @Test
  void equals_sameObject_shouldReturnTrue() {
    MultiformatDocumentParserParameters params = new MultiformatDocumentParserParameters();
    assertThat(params.equals(params)).isTrue();
  }

  @Test
  void equals_null_shouldReturnFalse() {
    MultiformatDocumentParserParameters params = new MultiformatDocumentParserParameters();
    assertThat(params.equals(null)).isFalse();
  }

  @Test
  void equals_differentClass_shouldReturnFalse() {
    MultiformatDocumentParserParameters params = new MultiformatDocumentParserParameters();
    assertThat(params.equals("not a params object")).isFalse();
  }

  @Test
  void equals_sameIncludeMetadata_shouldReturnTrue() {
    MultiformatDocumentParserParameters params1 = new MultiformatDocumentParserParameters();
    MultiformatDocumentParserParameters params2 = new MultiformatDocumentParserParameters();
    assertThat(params1).isEqualTo(params2);
  }

  @Test
  void equals_differentIncludeMetadata_shouldReturnFalse() throws Exception {
    MultiformatDocumentParserParameters params1 = new MultiformatDocumentParserParameters();
    MultiformatDocumentParserParameters params2 = new MultiformatDocumentParserParameters();
    java.lang.reflect.Field field = params2.getClass().getDeclaredField("includeMetadata");
    field.setAccessible(true);
    field.set(params2, true);
    assertThat(params1).isNotEqualTo(params2);
  }

  @Test
  void hashCode_sameValues_shouldBeEqual() {
    MultiformatDocumentParserParameters params1 = new MultiformatDocumentParserParameters();
    MultiformatDocumentParserParameters params2 = new MultiformatDocumentParserParameters();
    assertThat(params1.hashCode()).isEqualTo(params2.hashCode());
  }

  @Test
  void hashCode_differentValues_shouldDiffer() throws Exception {
    MultiformatDocumentParserParameters params1 = new MultiformatDocumentParserParameters();
    MultiformatDocumentParserParameters params2 = new MultiformatDocumentParserParameters();
    java.lang.reflect.Field field = params2.getClass().getDeclaredField("includeMetadata");
    field.setAccessible(true);
    field.set(params2, true);
    assertThat(params1.hashCode()).isNotEqualTo(params2.hashCode());
  }
}
