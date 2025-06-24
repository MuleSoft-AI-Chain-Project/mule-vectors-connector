package org.mule.extension.vectors.internal.storage.local;

import org.mule.extension.vectors.internal.config.StorageConfiguration;
import org.mule.extension.vectors.internal.connection.storage.local.LocalStorageConnection;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalStorage {
    public final String fullPath;
    private final LocalStorageConnection storageConnection;

    public LocalStorage(StorageConfiguration storageConfiguration, LocalStorageConnection storageConnection, String contextPath) {
        this.storageConnection = storageConnection;
        this.fullPath = storageConnection.getWorkingDir() != null ? storageConnection.getWorkingDir() + "/" + contextPath : contextPath;
    }

    public InputStream loadFile(Path path) {
        return storageConnection.loadFile(path);
    }

    public List<Path> listFiles(String directory) {
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            return paths.filter(Files::isRegularFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error while getting files from " + directory, e);
        }
    }

    public static String parseDirectory(String path) {
        int lastSlash = path.lastIndexOf("/");
        return lastSlash == -1 ? path : path.substring(0, lastSlash);
    }

    public static String parseFileName(String path) {
        int lastSlash = path.lastIndexOf("/");
        return lastSlash == -1 ? path : path.substring(lastSlash + 1);
    }
}
