package org.mule.extension.vectors.internal.storage.local;

import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.FileInfo;
import org.mule.extension.vectors.internal.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LocalFileIterator implements FileIterator {
    private final LocalStorage localClient;
    private final String fullPath;
    private Iterator<Path> pathIterator;

    public LocalFileIterator(LocalStorage localClient, String directory) {
        this.localClient = localClient;
        this.fullPath = directory;
        this.pathIterator = null;
    }

    private void fetchNextPathIterator() {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(fullPath), Files::isRegularFile);
            pathIterator = stream.iterator();
        } catch (IOException e) {
            pathIterator = Collections.emptyIterator();
        }
    }

    private Iterator<Path> getPathIterator() {
        if (pathIterator == null) {
            fetchNextPathIterator();
        }
        return pathIterator;
    }

    @Override
    public boolean hasNext() {
        return getPathIterator().hasNext();
    }

    @Override
    public FileInfo next() {
        if (!hasNext()) throw new NoSuchElementException();
        Path path = getPathIterator().next();
        InputStream content = localClient.loadFile(path);
        String mimeType = null;
        try {
            mimeType = Files.probeContentType(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Fallback if mimeType is null
            if (mimeType == null) {
                mimeType = Utils.getMimeTypeFallback(path);
            }
        }
        return new FileInfo(content, path.toString(), LocalStorage.parseFileName(path.toString()), mimeType);
    }
} 
