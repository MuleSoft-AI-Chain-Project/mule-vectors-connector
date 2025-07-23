package org.mule.extension.vectors.internal.helper;

import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.api.metadata.*;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.streaming.Cursor;
import org.mule.runtime.api.streaming.CursorProvider;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.streaming.StreamingHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseHelperTest {

    @Test
    void createStoreResponse_shouldReturnResultWithAttributesAndOutput() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("foo", "bar");
        String response = "{\"status\":\"ok\"}";
        Result<InputStream, StoreResponseAttributes> result = ResponseHelper.createStoreResponse(response, attrs);
        assertThat(result.getAttributes().get().getOtherAttributes()).containsEntry("foo", "bar");
        assertThat(result.getOutput()).hasSameContentAs(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));
        assertThat(result.getMediaType().get()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getAttributesMediaType().get()).isEqualTo(MediaType.APPLICATION_JAVA);
    }

    @Test
    void createPageStoreResponse_shouldReturnListWithResult() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("foo", "bar");
        String response = "{\"status\":\"ok\"}";
        StreamingHelper streamingHelper = mock(StreamingHelper.class);
        CursorProvider<Cursor> cursorProvider = mock(CursorProvider.class);
        when(streamingHelper.resolveCursorProvider(any(InputStream.class))).thenReturn(cursorProvider);
        List<Result<CursorProvider<Cursor>, StoreResponseAttributes>> page = ResponseHelper.createPageStoreResponse(response, attrs, streamingHelper);
        assertThat(page).hasSize(1);
        Result<CursorProvider<Cursor>, StoreResponseAttributes> result = page.get(0);
        assertThat(result.getAttributes().get().getOtherAttributes()).containsEntry("foo", "bar");
        assertThat(result.getOutput()).isSameAs(cursorProvider);
        assertThat(result.getMediaType().get()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getAttributesMediaType().get()).isEqualTo(MediaType.APPLICATION_JAVA);
    }

    @Test
    void createEmbeddingResponse_shouldReturnResultWithAttributes() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("baz", 42);
        attrs.put("embeddingModelDimension", 1);
        String response = "embeddings";
        Result<InputStream, EmbeddingResponseAttributes> result = ResponseHelper.createEmbeddingResponse(response, attrs);
        assertThat(result.getAttributes().get().getOtherAttributes()).containsEntry("baz", 42);
        assertThat(result.getOutput()).hasSameContentAs(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));
        assertThat(result.getMediaType().get()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getAttributesMediaType().get()).isEqualTo(MediaType.APPLICATION_JAVA);
    }

    @Test
    void createFileResponse_shouldReturnResultWithAttributesAndBinaryMediaType() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("file", "yes");
        InputStream content = new ByteArrayInputStream(new byte[]{1,2,3});
        Result<InputStream, StorageResponseAttributes> result = ResponseHelper.createFileResponse(content, attrs);
        assertThat(result.getAttributes().get().getOtherAttributes()).containsEntry("file", "yes");
        assertThat(result.getOutput()).hasSameContentAs(new ByteArrayInputStream(new byte[]{1,2,3}));
        assertThat(result.getMediaType().get()).isEqualTo(MediaType.BINARY);
        assertThat(result.getAttributesMediaType().get()).isEqualTo(MediaType.APPLICATION_JAVA);
    }

    @Test
    void createParsedDocumentResponse_shouldReturnResultWithTextMediaType() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("doc", "parsed");
        String response = "parsed text";
        Result<InputStream, TransformResponseAttributes> result = ResponseHelper.createParsedDocumentResponse(response, attrs);
        assertThat(result.getAttributes().get().getOtherAttributes()).containsEntry("doc", "parsed");
        assertThat(result.getOutput()).hasSameContentAs(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));
        assertThat(result.getMediaType().get()).isEqualTo(MediaType.TEXT);
        assertThat(result.getAttributesMediaType().get()).isEqualTo(MediaType.APPLICATION_JAVA);
    }

    @Test
    void createChunkedTextResponse_shouldReturnResultWithJsonMediaType() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("chunk", 1);
        String response = "chunked";
        Result<InputStream, TransformResponseAttributes> result = ResponseHelper.createChunkedTextResponse(response, attrs);
        assertThat(result.getAttributes().get().getOtherAttributes()).containsEntry("chunk", 1);
        assertThat(result.getOutput()).hasSameContentAs(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));
        assertThat(result.getMediaType().get()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(result.getAttributesMediaType().get()).isEqualTo(MediaType.APPLICATION_JAVA);
    }

    @Test
    void createPageFileResponse_shouldReturnListWithResult() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("foo", "bar");
        InputStream content = new ByteArrayInputStream(new byte[]{1,2,3});
        StreamingHelper streamingHelper = mock(StreamingHelper.class);
        CursorProvider<Cursor> cursorProvider = mock(CursorProvider.class);
        when(streamingHelper.resolveCursorProvider(content)).thenReturn(cursorProvider);
        List<Result<CursorProvider<Cursor>, StorageResponseAttributes>> page = ResponseHelper.createPageFileResponse(content, attrs, streamingHelper);
        assertThat(page).hasSize(1);
        Result<CursorProvider<Cursor>, StorageResponseAttributes> result = page.get(0);
        assertThat(result.getAttributes().get().getOtherAttributes()).containsEntry("foo", "bar");
        assertThat(result.getOutput()).isSameAs(cursorProvider);
        assertThat(result.getMediaType().get()).isEqualTo(MediaType.BINARY);
        assertThat(result.getAttributesMediaType().get()).isEqualTo(MediaType.APPLICATION_JAVA);
    }

    @Test
    void createProcessedMediaResponse_shouldReturnResultWithBinaryMediaType() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("media", "done");
        InputStream content = new ByteArrayInputStream(new byte[]{4,5,6});
        Result<InputStream, TransformResponseAttributes> result = ResponseHelper.createProcessedMediaResponse(content, attrs);
        assertThat(result.getAttributes().get().getOtherAttributes()).containsEntry("media", "done");
        assertThat(result.getOutput()).hasSameContentAs(new ByteArrayInputStream(new byte[]{4,5,6}));
        assertThat(result.getMediaType().get()).isEqualTo(MediaType.BINARY);
        assertThat(result.getAttributesMediaType().get()).isEqualTo(MediaType.APPLICATION_JAVA);
    }
} 
