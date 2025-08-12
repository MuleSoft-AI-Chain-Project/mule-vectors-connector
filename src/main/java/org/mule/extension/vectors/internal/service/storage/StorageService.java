package org.mule.extension.vectors.internal.service.storage;

import org.mule.extension.vectors.internal.data.file.FileInfo;
import org.mule.extension.vectors.internal.storage.FileIterator;

public interface StorageService {
    FileInfo getFile(String path);
    FileIterator getFileIterator(String contextPath);
} 
