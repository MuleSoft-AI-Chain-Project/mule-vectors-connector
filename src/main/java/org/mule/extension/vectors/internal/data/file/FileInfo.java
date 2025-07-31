package org.mule.extension.vectors.internal.data.file;

import java.io.InputStream;
import java.util.Map;

public class FileInfo {

  private InputStream content;
  private String path;
  private String fileName;
  private String mimeType;
  private Map<String, Object> metadata;

  public FileInfo(InputStream content, String path, String fileName) {
    this.content = content;
    this.path = path;
    this.fileName = fileName;
  }

  public FileInfo(InputStream content, String path, String fileName, String mimeType) {
    this(content, path, fileName);
    this.mimeType = mimeType;
  }

  public FileInfo(InputStream content, String path, String fileName, Map<String, Object> metadata) {
    this(content, path, fileName);
    this.metadata = metadata;
  }

  public FileInfo(InputStream content, String path, String fileName, String mimeType, Map<String, Object> metadata) {
    this(content, path, fileName, mimeType);
    this.metadata = metadata;
  }

  public InputStream getContent() {
    return content;
  }

  public String getPath() {
    return path;
  }

  public String getFileName() {
    return fileName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }
}
