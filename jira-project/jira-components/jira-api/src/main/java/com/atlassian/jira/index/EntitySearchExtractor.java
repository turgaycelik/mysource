package com.atlassian.jira.index;

import java.util.Set;

import com.atlassian.annotations.ExperimentalApi;

import org.apache.lucene.document.Document;

/**
 * Provides ability to add fields to Document during indexing
 *
 * @since 6.2
 */
@ExperimentalApi
public interface EntitySearchExtractor<T>
{
    @ExperimentalApi
    interface Context<T>
    {

        T getEntity();

        /**
         * currently as for 6.2 one of issues, comments, changes See  constant in {@link
         * com.atlassian.jira.issue.search.SearchProviderFactory} for available indexes
         *
         * @return index name
         */
        String getIndexName();

    }

    /**
     * Extracts fields from document provided by {@link Context} and adds them to document.
     * All the filed names that were added to documents as indexed to document must be returned as output of this method
     * @param ctx context for this operation
     * @param doc lucene document to which values should be added
     * @return ids of the document fields that were added by call of this method
     */
    Set<String> indexEntity(Context<T> ctx, Document doc);
}
