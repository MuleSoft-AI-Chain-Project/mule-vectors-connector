package org.mule.extension.vectors.internal.storage.local;

import org.mule.extension.vectors.internal.storage.FileIterator;
import org.mule.extension.vectors.internal.data.file.File;
import org.mule.extension.vectors.internal.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class LocalFileIterator implements FileIterator {
    private final LocalStorage localClient;
    private final Iterator<Path> pathIterator;

    public LocalFileIterator(LocalStorage localClient, List<Path> files) {
        this.localClient = localClient;
        this.pathIterator = files.iterator();
    }

    @Override
    public boolean hasNext() {
        return pathIterator.hasNext();
    }

    @Override
    public File next() {
        if (hasNext()) {
            Path path = pathIterator.next();
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
            return new File(content, path.toString(), path.getFileName().toString(), mimeType);
        }
        return null;
    }
} 
