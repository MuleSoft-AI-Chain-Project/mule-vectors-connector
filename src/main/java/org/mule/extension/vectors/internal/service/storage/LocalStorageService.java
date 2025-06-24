package org.mule.extension.vectors.internal.service.storage;

import org.mule.extension.vectors.internal.storage.local.LocalStorage;
import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.storage.local.LocalFileIterator;
import org.mule.extension.vectors.internal.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
        String mimeType = null;
        try {
            mimeType = Files.probeContentType(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Fallback if mimeType is null
            if (mimeType == null) {
                mimeType = Utils.getMimeTypeFallback(filePath);
            }
        }
        return new File(content, path, LocalStorage.parseFileName(path), mimeType);
    }

    @Override
    public FileIterator getFileIterator(String directory) {
        List<Path> files = localClient.listFiles(directory);
        return new LocalFileIterator(localClient, files);
    }
} 
