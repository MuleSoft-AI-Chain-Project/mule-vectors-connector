package org.mule.extension.vectors.internal.storage.local;

import dev.langchain4j.data.document.BlankDocumentException;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer;
import org.mule.extension.vectors.internal.config.DocumentConfiguration;
import org.mule.extension.vectors.internal.connection.storage.local.LocalStorageConnection;
import org.mule.extension.vectors.internal.constant.Constants;
import org.mule.extension.vectors.internal.error.MuleVectorsErrorType;
import org.mule.extension.vectors.internal.storage.BaseStorage;
import org.mule.extension.vectors.internal.util.MetadataUtils;
import org.mule.extension.vectors.internal.util.Utils;
import org.mule.runtime.extension.api.exception.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalStorage extends BaseStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorage.class);

  private final String fullPath;

  private List<Path> pathList;
  private Iterator<Path> pathIterator;

  private Iterator<Path> getPathIterator() {
    if (pathList == null) {  // Only load files if not already loaded
      try (Stream<Path> paths = Files.walk(Paths.get(fullPath))) {
        // Collect all files as a list
        pathList = paths.filter(Files::isRegularFile).collect(Collectors.toList());
        // Create an iterator for the list of files
        pathIterator = pathList.iterator();
      } catch (IOException e) {
        throw new ModuleException(
            String.format("Error while getting document from %s.", fullPath),
            MuleVectorsErrorType.STORAGE_SERVICES_FAILURE,
            e);
      }
    }
    return pathIterator;
  }

  public LocalStorage(DocumentConfiguration documentConfiguration, LocalStorageConnection storageConnection, String contextPath, String fileType) {

    super(documentConfiguration, contextPath, fileType);
    this.fullPath = storageConnection.getWorkingDir() != null ? storageConnection.getWorkingDir() + "/" + contextPath : contextPath;
  }

  // Override hasNext to check if there are files left to process
  @Override
  public boolean hasNext() {
    return getPathIterator() != null && getPathIterator().hasNext();
  }

  // Override next to return the next document
  @Override
  public Document next() {
    if (hasNext()) {
      Path path = getPathIterator().next();
      LOGGER.debug("File: " + path.getFileName().toString());
      Document document;
      try {
        document = getDocument(new DefaultResourceLoader().getResource(path.toString()));
      } catch(BlankDocumentException bde) {

        LOGGER.warn(String.format("BlankDocumentException: Error while parsing document %s.", path.toString()));
        throw bde;
      } catch (Exception e) {

        throw new ModuleException(
            String.format("Error while parsing document %s.", path.toString()),
            MuleVectorsErrorType.DOCUMENT_PARSING_FAILURE,
            e);
      }
      MetadataUtils.addMetadataToDocument(document, fileType, path.getFileName().toString());
      return document;
    }
    throw new IllegalStateException("No more files to iterate");
  }

  public Document getSingleDocument() {

    Path path = Paths.get(fullPath);

    Document document;
    switch (fileType) {
      case Constants.FILE_TYPE_CRAWL:
      case Constants.FILE_TYPE_TEXT:
      case Constants.FILE_TYPE_ANY:
        document = getDocument(new DefaultResourceLoader().getResource(path.toString()));
        MetadataUtils.addMetadataToDocument(document, fileType, Utils.getFileNameFromPath(fullPath));
        break;
      case Constants.FILE_TYPE_URL:
        document = loadUrlDocument(contextPath);
        break;
      default:
        throw new IllegalArgumentException("Unsupported File Type: " + fileType);
    }
    return document;
  }

  private Document loadUrlDocument(String contextPath) {

    Document document;
    try {
      URL url = new URL(contextPath);
      Document htmlDocument = new Document(UrlDocumentLoader.load(url, new TextDocumentParser()).toString());
      HtmlToTextDocumentTransformer transformer = new HtmlToTextDocumentTransformer(null, null, true);
      document = new Document(transformer.transform(UrlDocumentLoader.load(url, new TextDocumentParser())).toString());
      document.getMetadata().put(Constants.METADATA_KEY_URL, contextPath);
      MetadataUtils.addMetadataToDocument(document, Constants.FILE_TYPE_URL, "");
    } catch (MalformedURLException e) {
      throw new RuntimeException("Invalid URL: " + contextPath, e);
    }
    return document;
  }
}
