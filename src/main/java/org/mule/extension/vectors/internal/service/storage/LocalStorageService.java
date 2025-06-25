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
    public File getFile(String contextPath) {
        InputStream content = localClient.loadFile(contextPath);
        return new File(content, contextPath, LocalStorage.parseFileName(contextPath));
    }

    @Override
    public FileIterator getFileIterator(String contextPath)
    {
        String fullPath = this.localClient.getConnection().getWorkingDir() != null ? this.localClient.getConnection().getWorkingDir() + "/" + contextPath : contextPath;
        return new LocalFileIterator(localClient, fullPath);
    }
} 
