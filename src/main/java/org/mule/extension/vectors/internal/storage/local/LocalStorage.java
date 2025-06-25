package org.mule.extension.vectors.internal.storage.local;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.local.LocalStorageConnection;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalStorage {
    private final LocalStorageConnection storageConnection;

    public LocalStorage(StorageConfiguration storageConfiguration, LocalStorageConnection storageConnection) {
        this.storageConnection = storageConnection;

    }
public LocalStorageConnection getConnection(){
        return  this.storageConnection;
}
    public InputStream loadFile(String contextPath) {
        String fullPath = storageConnection.getWorkingDir() != null ? storageConnection.getWorkingDir() + "/" + contextPath : contextPath;
        return loadSpecificFile(Path.of(fullPath));

    }
    public InputStream loadSpecificFile(Path path) {

        if (!Files.isRegularFile(path, new LinkOption[0])) {
            throw new IllegalArgumentException(String.format("'%s' is not a file", new Object[]{path}));
        } else {
            try {
                return  Files.newInputStream(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String parseFileName(String path) {
        int lastSlash = path.lastIndexOf("/");
        return lastSlash == -1 ? path : path.substring(lastSlash + 1);
    }
}
