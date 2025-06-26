package org.mule.extension.vectors.internal.service.transform;

import org.mule.extension.vectors.internal.transform.TextSegment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an unstructured piece of text that usually corresponds to a content of a single file.
 * This text could originate from various sources such as a text file, PDF, DOCX, or a web page (HTML).
 * Each document may have associated metadata including its source, owner, creation date, etc.
 */
public interface Document {

    /**
     * Common metadata key for the name of the file from which the document was loaded.
     */
    String FILE_NAME = "file_name";
    /**
     * Common metadata key for the absolute path of the directory from which the document was loaded.
     */
    String ABSOLUTE_DIRECTORY_PATH = "absolute_directory_path";
    /**
     * Common metadata key for the URL from which the document was loaded.
     */
    String URL = "url";

    /**
     * Returns the text of this document.
     *
     * @return the text.
     */
    String text();

    /**
     * Returns the metadata associated with this document.
     *
     * @return the metadata (never null).
     */
    Map<String, Object> metadata();

    /**
     * Builds a {@link TextSegment} from this document.
     *
     * @return a {@link TextSegment}
     */
    default TextSegment toTextSegment() {
        Map<String, Object> metaCopy = new HashMap<>(metadata());
        if (metaCopy.containsKey("index")) {
            return TextSegment.from(text(), metaCopy);
        } else {
            metaCopy.put("index", "0");
            return TextSegment.from(text(), metaCopy);
        }
    }

    /**
     * Creates a new Document from the given text.
     * <p>The created document will have empty metadata.</p>
     * @param text the text of the document.
     * @return a new Document.
     */
    static Document from(String text) {
        return new DefaultDocument(text);
    }

    /**
     * Creates a new Document from the given text and metadata.
     * @param text the text of the document.
     * @param metadata the metadata of the document.
     * @return a new Document.
     */
    static Document from(String text, Map<String, Object> metadata) {
        return new DefaultDocument(text, metadata);
    }

    /**
     * Creates a new Document from the given text (alias for from).
     * <p>The created document will have empty metadata.</p>
     * @param text the text of the document.
     * @return a new Document.
     */
    static Document document(String text) {
        return from(text);
    }

    /**
     * Creates a new Document from the given text and metadata (alias for from).
     * @param text the text of the document.
     * @param metadata the metadata of the document.
     * @return a new Document.
     */
    static Document document(String text, Map<String, Object> metadata) {
        return from(text, metadata);
    }
} 