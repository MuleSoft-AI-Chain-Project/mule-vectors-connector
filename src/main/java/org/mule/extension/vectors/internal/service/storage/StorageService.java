package org.mule.extension.vectors.internal.service.storage;

import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.storage.FileIterator;

public interface StorageService {
    File getFile(String path);
    FileIterator getFileIterator(String directory);
} 