package org.mule.extension.vectors.internal.util;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.junit.jupiter.api.Test;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.data.media.Media;

import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetadataUtilsTest {

    @Test
    void getIngestionMetadata_shouldContainAllKeys() {
        HashMap<String, Object> meta = MetadataUtils.getIngestionMetadata();
        assertThat(meta).containsKeys(
                Constants.METADATA_KEY_SOURCE_ID,
                Constants.METADATA_KEY_INGESTION_DATETIME,
                Constants.METADATA_KEY_INGESTION_TIMESTAMP
        );
    }

    @Test
    void getSourceDisplayName_shouldReturnCorrectDisplayName() {
        Metadata meta = new Metadata();
        meta.put("absolute_directory_path", "/foo/bar");
        meta.put("file_name", "baz.txt");
        assertThat(MetadataUtils.getSourceDisplayName(meta)).isEqualTo("/foo/bar/baz.txt");

        Metadata meta2 = new Metadata();
        meta2.put("url", "http://example.com");
        assertThat(MetadataUtils.getSourceDisplayName(meta2)).isEqualTo("http://example.com");

        Metadata meta3 = new Metadata();
        meta3.put("source", "src");
        assertThat(MetadataUtils.getSourceDisplayName(meta3)).isEqualTo("src");

        Metadata meta4 = new Metadata();
        meta4.put("title", "title");
        assertThat(MetadataUtils.getSourceDisplayName(meta4)).isEqualTo("title");
    }

    @Test
    void addMetadataToDocument_shouldAddFileType() {
        Document doc = mock(Document.class);
        Metadata meta = new Metadata();
        when(doc.metadata()).thenReturn(meta);
        MetadataUtils.addMetadataToDocument(doc, "text");
        assertThat(meta.getString(Constants.METADATA_KEY_FILE_TYPE)).isEqualTo("text");
    }

    @Test
    void addMetadataToDocument_withFileName_shouldAddBoth() {
        Document doc = mock(Document.class);
        Metadata meta = new Metadata();
        when(doc.metadata()).thenReturn(meta);
        MetadataUtils.addMetadataToDocument(doc, "text", "foo.txt");
        assertThat(meta.getString(Constants.METADATA_KEY_FILE_TYPE)).isEqualTo("text");
        assertThat(meta.getString(Constants.METADATA_KEY_FILE_NAME)).isEqualTo("foo.txt");
    }

    @Test
    void addImageMetadataToMedia_shouldAddAllRelevantMetadata() {
        Media media = mock(Media.class);
        dev.langchain4j.data.image.Image image = mock(dev.langchain4j.data.image.Image.class);
        Metadata meta = new Metadata();
        when(media.metadata()).thenReturn(meta);
        when(media.hasImage()).thenReturn(true);
        when(media.image()).thenReturn(image);
        when(image.mimeType()).thenReturn("image/png");
        when(image.url()).thenReturn(URI.create("file:///foo/bar/baz.png"));
        MetadataUtils.addImageMetadataToMedia(media, "image");
        assertThat(meta.getString(Constants.METADATA_KEY_MEDIA_TYPE)).isEqualTo("image");
        assertThat(meta.getString(Constants.METADATA_KEY_MIME_TYPE)).isEqualTo("image/png");
        assertThat(meta.getString(Constants.METADATA_KEY_FILE_TYPE)).isEqualTo("png");
        assertThat(meta.getString(Constants.METADATA_KEY_SOURCE)).isEqualTo("file:///foo/bar/baz.png");
        assertThat(meta.getString(Constants.METADATA_KEY_ABSOLUTE_DIRECTORY_PATH)).isEqualTo("/foo/bar");
        assertThat(meta.getString(Constants.METADATA_KEY_FILE_NAME)).isEqualTo("baz.png");
    }

    @Test
    void addImageMetadataToMedia_shouldHandleEmptyMediaTypeAndNoImage() {
        Media media = mock(Media.class);
        Metadata meta = new Metadata();
        when(media.metadata()).thenReturn(meta);
        when(media.hasImage()).thenReturn(false);
        MetadataUtils.addImageMetadataToMedia(media, "");
        assertThat(meta.getString(Constants.METADATA_KEY_MEDIA_TYPE)).isNull();
        assertThat(meta.getString(Constants.METADATA_KEY_MIME_TYPE)).isNull();
        assertThat(meta.getString(Constants.METADATA_KEY_FILE_TYPE)).isNull();
        assertThat(meta.getString(Constants.METADATA_KEY_SOURCE)).isNull();
        assertThat(meta.getString(Constants.METADATA_KEY_ABSOLUTE_DIRECTORY_PATH)).isNull();
        assertThat(meta.getString(Constants.METADATA_KEY_FILE_NAME)).isNull();
    }
} 