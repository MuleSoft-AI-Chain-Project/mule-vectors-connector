package org.mule.extension.vectors.internal.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import org.json.JSONObject;
import org.mule.extension.vectors.internal.helper.parameter.RemoveFilterParameters;
import org.mule.extension.vectors.internal.helper.parameter.SearchFilterParameters;
import org.mule.extension.vectors.internal.store.BaseStoreService;

import java.util.Iterator;
import java.util.List;

/**
 * Defines the contract for all vector store operations.
 * This interface provides a common set of methods for interacting with any underlying vector store implementation,
 * ensuring a consistent API for the operations layer.
 */
public interface VectorStoreService {

    /**
     * Queries the vector store to find the most relevant text segments based on a query embedding.
     *
     * @param textSegments       The original text segments associated with the query.
     * @param embeddings         The list of query embeddings (typically one).
     * @param maxResults         The maximum number of results to return.
     * @param minScore           The minimum similarity score for a result to be included.
     * @param searchFilterParams The parameters for filtering the search based on metadata.
     * @return A JSONObject containing the query results.
     */
    JSONObject query(List<TextSegment> textSegments,
                     List<Embedding> embeddings,
                     Number maxResults,
                     Double minScore,
                     SearchFilterParameters searchFilterParams);

    /**
     * Adds a list of text segments and their corresponding embeddings to the vector store.
     *
     * @param embeddings   The list of embeddings to add.
     * @param textSegments The list of text segments to add.
     * @return A list of IDs for the embeddings that were added.
     */
    List<String> add(List<Embedding> embeddings, List<TextSegment> textSegments);

    /**
     * Removes entries from the vector store based on a filter.
     *
     * @param removeFilterParams The parameters defining what to remove (e.g., by ID or metadata filter).
     */
    void remove(RemoveFilterParameters removeFilterParams);

} 
