package org.mule.extension.vectors.internal.data.file;

import java.io.InputStream;

public class File {

  private InputStream content;
  private String path;
  private String fileName;

  public File(InputStream content, String path, String fileName) {
    this.content = content;
    this.path = path;
    this.fileName = fileName;
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
}
