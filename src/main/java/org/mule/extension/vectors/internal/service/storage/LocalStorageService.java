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
    public File getFile(String contextPath) {
        String fullPath = localClient.getConnection().getWorkingDir() != null ?  localClient.getConnection().getWorkingDir() + "/" + contextPath : contextPath;
        InputStream content = localClient.loadFile(Path.of(fullPath));
        String mimeType = null;
        try {
            mimeType = Files.probeContentType(Path.of(fullPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Fallback if mimeType is null
            if (mimeType == null) {
                mimeType = Utils.getMimeTypeFallback(Path.of(fullPath));
            }
        }
        return new File(content, contextPath, LocalStorage.parseFileName(contextPath), mimeType);
    }

    @Override
    public FileIterator getFileIterator(String contextPath)
    {
        String fullPath = this.localClient.getConnection().getWorkingDir() != null ? this.localClient.getConnection().getWorkingDir() + "/" + contextPath : contextPath;
        return new LocalFileIterator(localClient, fullPath);
    }
} 
