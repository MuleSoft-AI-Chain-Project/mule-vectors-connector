package org.mule.extension.vectors.internal.connection.storage.local;

import static org.assertj.core.api.Assertions.*;

import org.mule.extension.vectors.internal.constant.Constants;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalStorageConnectionTest {

  @TempDir
  Path tempDir;

  @Test
  void constructor_assignsWorkingDir() {
    LocalStorageConnection conn = new LocalStorageConnection("/tmp");
    assertThat(conn.getWorkingDir()).isEqualTo("/tmp");
    assertThat(conn.getStorageType()).isEqualTo(Constants.STORAGE_TYPE_LOCAL);
  }

  @Test
  void initialise_withValidDirectory() {
    LocalStorageConnection conn = new LocalStorageConnection(tempDir.toString());
    conn.initialise();
    assertThat(conn.getWorkingDir()).isEqualTo(tempDir.toString());
  }

  @Test
  void initialise_withNullDir_defaultsToHome() {
    LocalStorageConnection conn = new LocalStorageConnection(null);
    conn.initialise();
    assertThat(conn.getWorkingDir()).isNotNull();
  }

  @Test
  void initialise_withNonExistentDir_throwsException() {
    LocalStorageConnection conn = new LocalStorageConnection("/nonexistent/path/xyz123");
    assertThatThrownBy(conn::initialise).isInstanceOf(RuntimeException.class);
  }

  @Test
  void initialise_withFilePath_throwsException() throws Exception {
    Path file = tempDir.resolve("not-a-dir.txt");
    java.nio.file.Files.writeString(file, "content");
    LocalStorageConnection conn = new LocalStorageConnection(file.toString());
    assertThatThrownBy(conn::initialise).isInstanceOf(RuntimeException.class);
  }

  @Test
  void validate_withValidDir_doesNotThrow() {
    LocalStorageConnection conn = new LocalStorageConnection(tempDir.toString());
    conn.validate();
  }

  @Test
  void validate_withNullDir_doesNotThrow() {
    LocalStorageConnection conn = new LocalStorageConnection(null);
    conn.validate();
  }

  @Test
  void disconnect_isNoOp() {
    LocalStorageConnection conn = new LocalStorageConnection("/tmp");
    conn.disconnect();
    assertThat(conn.getWorkingDir()).isEqualTo("/tmp");
  }
}
