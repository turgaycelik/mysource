package com.atlassian.jira.issue.search;

import com.atlassian.annotations.PublicSpi;

import java.util.Comparator;

/**
 * Implementations of this interface are used to sort Lucene search results of Issue Documents.
 * <p/>
 * <strong>NOTE</strong>: instances of this interface are <strong>cached</strong> by Lucene and are
 * <strong>REUSED</strong> to sort multiple Lucene search results. The Comparator returned by the
 * {@link #getComparator()} method could be used by Lucene from multiple threads at once.
 * <p/>
 * Therefore, the implementations of this interface <strong>MUST</strong> implement the {@link Object#equals(Object)}
 * and {@link Object#hashCode()} methods correctly to ensure that Lucene can find the implementations of this class
 * in its cache and reuse it, rather than make the cache grow indefinitely. (Unfortunately the Lucene cache is rather
 * primitive at the moment, and is not bound).
 * <p/>
 * Also, ensure that the {@link Comparator} returned by the {@link #getComparator()} method is <strong>thread
 * safe</strong>.
 * <p/>
 * As instances of this and the {@link Comparator} returned by this object are cached and reused by Lucene to sort
 * multiple search results, the best thing to do is to ensure the implementations of this interface and the
 * {@link Comparator} that is returned <strong>are immutable</strong> and that the {@link #equals(Object)} and
 * {@link #hashCode()} methods respect the state of the object.
 */
@PublicSpi
public interface LuceneFieldSorter<T>
{
    /**
     * Get the constant that this field is indexed with.
     * @see com.atlassian.jira.issue.index.IssueDocument
     * @see com.atlassian.jira.issue.customfields.CustomFieldSearcher
     */
    String getDocumentConstant();

    /**
     * Convert the lucene document field back to the object that you wish to use to display it.
     * <p>
     * eg. '1000' -> Version 1.
     * <p>
     * This does the reverse of what {@link com.atlassian.jira.issue.index.IssueDocument} does.
     * <p>
     * For custom fields, the return value will be passed to
     * {@link com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor#getStatHtml(com.atlassian.jira.issue.fields.CustomField, Object, String)}
     *
     * @param   documentValue   The value of the field in the lucene index
     * @return  The value that will be passed to the display
     */
    T getValueFromLuceneField(String documentValue);

    /**
     * A comparator that can be used to order objects returned by {@link #getValueFromLuceneField(String)}.
     * <p/>
     * The Comparator <strong>must</strong> be reentrant as it could be used by Lucene from multiple threads at once.
     */
    Comparator<T> getComparator();

    /**
     * As this object is used as a key in a cache, this method <strong>must</strong> be provided and respect all internal state.
     * <p/>
     * See the class javadoc entry for more details.
     */
    boolean equals(Object obj);

    /**
     * As this object is used as a key in a cache, this method <strong>must</strong> be provided and respect all internal state.
     * <p/>
     * See the class javadoc entry for more details.
     */
    int hashCode();
}
