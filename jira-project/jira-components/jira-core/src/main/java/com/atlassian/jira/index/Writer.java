package com.atlassian.jira.index;

import com.atlassian.jira.util.Closeable;
import javax.annotation.Nonnull;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Collection;

/**
 * Partial interface of IndexWriter that contains only the methods we actually need.
 * <p>
 * Allows us to delegate much more easily and makes testing simpler. Also hides the
 * implementation details of IndexWriter interaction.
 */
interface Writer extends Closeable
{
    void addDocuments(@Nonnull Collection<Document> document) throws IOException;

    void deleteDocuments(@Nonnull Term identifyingTerm) throws IOException;

    void updateDocuments(@Nonnull Term identifyingTerm, @Nonnull Collection<Document> document) throws IOException;

    /**
     * Updates the document with the given {@code identifyingTerm} if and only if the value {@code optimisticLockField}
     * in the index is equal to the value of {@code document.get(optimisticLockField)}. This is useful for achieving
     * optimistic locking when updating the index (used in conjunction with a "version" or "updated date" field).
     *
     * @param identifyingTerm    a Term that uniquely identifies the document to be updated
     * @param document    a new Document
     * @param optimisticLockField a String containing a field name
     * @throws IOException
     * @since v5.2
     */
    void updateDocumentConditionally(@Nonnull Term identifyingTerm, @Nonnull Document document, @Nonnull String optimisticLockField) throws IOException;

    void optimize() throws IOException;

    void close();

    void commit();

}
