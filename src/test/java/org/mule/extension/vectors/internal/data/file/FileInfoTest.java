package org.mule.extension.vectors.internal.data.file;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class FileInfoTest {

  @Test
  void constructorAndGetters_basic() {
    InputStream content = new ByteArrayInputStream(new byte[] {1, 2, 3});
    FileInfo file = new FileInfo(content, "/foo/bar", "baz.txt");
    assertThat(file.getContent()).isSameAs(content);
    assertThat(file.getPath()).isEqualTo("/foo/bar");
    assertThat(file.getFileName()).isEqualTo("baz.txt");
    assertThat(file.getMimeType()).isNull();
    assertThat(file.getMetadata()).isNull();
  }

  @Test
  void constructorAndGetters_withMimeType() {
    InputStream content = new ByteArrayInputStream(new byte[] {4, 5, 6});
    FileInfo file = new FileInfo(content, "/foo/bar", "baz.txt", "text/plain");
    assertThat(file.getContent()).isSameAs(content);
    assertThat(file.getPath()).isEqualTo("/foo/bar");
    assertThat(file.getFileName()).isEqualTo("baz.txt");
    assertThat(file.getMimeType()).isEqualTo("text/plain");
    assertThat(file.getMetadata()).isNull();
  }

  @Test
  void constructorAndGetters_withMetadata() {
    InputStream content = new ByteArrayInputStream(new byte[] {7, 8, 9});
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("foo", "bar");
    FileInfo file = new FileInfo(content, "/foo/bar", "baz.txt", metadata);
    assertThat(file.getContent()).isSameAs(content);
    assertThat(file.getPath()).isEqualTo("/foo/bar");
    assertThat(file.getFileName()).isEqualTo("baz.txt");
    assertThat(file.getMimeType()).isNull();
    assertThat(file.getMetadata()).isEqualTo(metadata);
  }

  @Test
  void constructorAndGetters_withMimeTypeAndMetadata() {
    InputStream content = new ByteArrayInputStream(new byte[] {10, 11, 12});
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("key", 123);
    FileInfo file = new FileInfo(content, "/foo/bar", "baz.txt", "application/pdf", metadata);
    assertThat(file.getContent()).isSameAs(content);
    assertThat(file.getPath()).isEqualTo("/foo/bar");
    assertThat(file.getFileName()).isEqualTo("baz.txt");
    assertThat(file.getMimeType()).isEqualTo("application/pdf");
    assertThat(file.getMetadata()).isEqualTo(metadata);
  }

  @Test
  void getters_nullFields() {
    FileInfo file = new FileInfo(null, null, null, (String) null, null);
    assertThat(file.getContent()).isNull();
    assertThat(file.getPath()).isNull();
    assertThat(file.getFileName()).isNull();
    assertThat(file.getMimeType()).isNull();
    assertThat(file.getMetadata()).isNull();
  }
}
