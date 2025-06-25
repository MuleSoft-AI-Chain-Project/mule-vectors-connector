package org.mule.extension.vectors.internal.service.storage;

import org.mule.extension.vectors.internal.storage.local.LocalStorage;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.storage.local.LocalFileIterator;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LocalStorageService implements StorageService {
    private final LocalStorage localClient;

    public LocalStorageService(LocalStorage localClient) {
        this.localClient = localClient;
    }

    @Override
    public File getFile(String path) {
        Path filePath = Paths.get(path);
        InputStream content = localClient.loadFile(filePath);
        return new File(content, path, LocalStorage.parseFileName(path));
    }

    @Override
    public FileIterator getFileIterator(String directory) {
        return new LocalFileIterator(localClient, directory);
    }
} 