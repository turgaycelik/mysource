package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.annotations.ExperimentalApi;

import java.util.Collections;
import java.util.List;

/**
 * A match handler that assumes all values it will only ever see one
 * value per document.  This allows it to optimise the storage somewhat
 * by avoiding the use of more complicated data structures to contain
 * the values.
 *
 * @since v5.1
 */
@ExperimentalApi
public class SingleValueMatchHandler implements MatchHandler
{
    private static final List<String> NULL_SINGLETON = Collections.singletonList(null);

    private final List<String>[] docToTerms;

    // Note: The matcher will feed up the same term value over repeatedly
    // for multiple docs before moving on to the next term value.  These
    // two fields allow us to reuse singletons for as long
    private Object previousTermValue = null;
    private List<String> currentSingleton = NULL_SINGLETON;

    @SuppressWarnings("unchecked")
    public SingleValueMatchHandler(int maxdoc)
    {
        this.docToTerms = new List[maxdoc];
    }

    public void handleMatchedDocument(int doc, String termValue)
    {
        //noinspection StringEquality
        if (previousTermValue != termValue)
        {
            previousTermValue = termValue;
            currentSingleton = Collections.singletonList(termValue);
        }
        docToTerms[doc] = currentSingleton;
    }

    /**
     * Get the results from the match handler.  Every element is guaranteed
     * to be either <tt>null</tt> (meaning that document had no value for the
     * field) or an immutable <tt>List</tt> containing exactly one value
     * ({@link Collections#singletonList(Object)}).
     *
     * @return the resulting matches
     */
    public List<String>[] getResults()
    {
        return docToTerms;
    }
}
