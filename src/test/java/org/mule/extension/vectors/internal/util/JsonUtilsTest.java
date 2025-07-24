package org.mule.extension.vectors.internal.util;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.media.Media;
import org.mule.extension.vectors.internal.service.store.VectorStoreRow;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.segment.TextSegment;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class JsonUtilsTest {

  @Test
  void jsonObjectCollectionToJsonArray_shouldConvertList() {
    JSONObject obj1 = new JSONObject().put("a", 1);
    JSONObject obj2 = new JSONObject().put("b", 2);
    JSONArray arr = JsonUtils.jsonObjectCollectionToJsonArray(Arrays.asList(obj1, obj2));
    assertThat(arr.length()).isEqualTo(2);
    assertThat(arr.getJSONObject(0).getInt("a")).isEqualTo(1);
    assertThat(arr.getJSONObject(1).getInt("b")).isEqualTo(2);
  }

  @Test
  void createIngestionStatusObject_shouldContainStatusAndIds() {
    List<String> ids = Arrays.asList("id1", "id2");
    JSONObject obj = JsonUtils.createIngestionStatusObject("srcId", ids);
    assertThat(obj.getString(Constants.JSON_KEY_STATUS)).isEqualTo(Constants.OPERATION_STATUS_UPDATED);
    assertThat(obj.getString(Constants.JSON_KEY_SOURCE_ID)).isEqualTo("srcId");
    assertThat(obj.getJSONArray(Constants.JSON_KEY_EMBEDDING_IDS)).containsExactly("id1", "id2");
  }

  @Test
  void docToTextSegmentsJson_shouldReturnTextSegments() {
    Metadata metadata = Metadata.from("foo", "bar");
    TextSegment segment = new TextSegment("hello world", metadata);
    Document doc = mock(Document.class);
    when(doc.toTextSegment()).thenReturn(segment);
    JSONObject obj = JsonUtils.docToTextSegmentsJson(doc);
    JSONArray segments = obj.getJSONArray(Constants.JSON_KEY_TEXT_SEGMENTS);
    assertThat(segments.length()).isEqualTo(1);
    assertThat(segments.getJSONObject(0).getString(Constants.JSON_KEY_TEXT)).isEqualTo("hello world");
    assertThat(segments.getJSONObject(0).getJSONObject(Constants.JSON_KEY_METADATA).getString("foo")).isEqualTo("bar");
  }

  @Test
  void docToTextSegmentsJson_withSplit_shouldReturnSegments() {
    Metadata metadata = Metadata.from("foo", "bar");
    TextSegment segment = new TextSegment("hello world", metadata);
    Document doc = mock(Document.class);
    when(doc.toTextSegment()).thenReturn(segment);
    when(doc.text()).thenReturn("hello world");
    when(doc.metadata()).thenReturn(metadata);
    JSONObject obj = JsonUtils.docToTextSegmentsJson(doc, 5, 1);
    JSONArray segments = obj.getJSONArray(Constants.JSON_KEY_TEXT_SEGMENTS);
    assertThat(segments.length()).isGreaterThanOrEqualTo(1);
    assertThat(segments.getJSONObject(0).getString(Constants.JSON_KEY_TEXT)).contains("hello");
  }

  @Test
  void mediaToJson_shouldReturnBase64AndMetadata() {
    Media media = mock(Media.class);
    Image image = mock(Image.class);
    when(media.hasImage()).thenReturn(true);
    when(media.image()).thenReturn(image);
    when(image.base64Data()).thenReturn("base64data");
    when(media.metadata()).thenReturn(Metadata.from("foo", "bar"));
    JSONObject obj = JsonUtils.mediaToJson(media);
    assertThat(obj.getString(Constants.JSON_KEY_BASE64DATA)).isEqualTo("base64data");
    assertThat(obj.getJSONObject(Constants.JSON_KEY_METADATA).getString("foo")).isEqualTo("bar");
  }

  @Test
  void rowToJson_shouldReturnRowFields() {
    TextSegment segment = new TextSegment("text", Metadata.from("foo", "bar"));
    VectorStoreRow<TextSegment> row = mock(VectorStoreRow.class);
    Embedding embedding = mock(Embedding.class);
    when(embedding.vector()).thenReturn(new float[] {1.0f, 2.0f});
    when(row.getEmbedding()).thenReturn(embedding);
    when(row.getEmbedded()).thenReturn(segment);
    when(row.getId()).thenReturn("rowId");
    JSONObject obj = JsonUtils.rowToJson(row);
    assertThat(obj.getJSONArray(Constants.JSON_KEY_EMBEDDINGS).getFloat(0)).isEqualTo(1.0f);
    assertThat(obj.getString(Constants.JSON_KEY_TEXT)).isEqualTo("text");
    assertThat(obj.getString(Constants.JSON_KEY_EMBEDDING_ID)).isEqualTo("rowId");
    assertThat(obj.getJSONObject(Constants.JSON_KEY_METADATA).getString("foo")).isEqualTo("bar");
  }
}
