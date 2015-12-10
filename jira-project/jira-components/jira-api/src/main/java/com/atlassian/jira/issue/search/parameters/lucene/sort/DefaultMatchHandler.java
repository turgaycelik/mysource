package com.atlassian.jira.issue.search.parameters.lucene.sort;

import com.atlassian.annotations.ExperimentalApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The default match handler used by {@link JiraLuceneFieldFinder#getMatches(org.apache.lucene.index.IndexReader, String)}.
 *
 * Builds an array of collections of strings, where the array index is
 * the document number and the collection contains the values for that
 * term.  In JIRA's terms, the array index is a key for looking up an
 * issue and the collection contains the values assigned to the field
 * we are matching against.
 *
 * This array built here is a memory eating monster and we take special care to eat as little as possible.
 * This matcher labours under the assumption that nearly all documents have single values for most terms,
 * even in the case of multi-valued fields, such as component or fixVersion, most documents have only a single value,
 * often the empty value, "-1".
 *
 * We use a shared singleton for any single values and only build a mutable collection once we go past a single value.
 * This has no performance cost even in the case where there are > 1 values, aside from the size() == 1 comparison.
 *
 *
 * @since v5.1
 */
@ExperimentalApi
public class DefaultMatchHandler implements MatchHandler
{
    private static final List<String> NULL_SINGLETON = Collections.singletonList(null);
    private final List<String>[] docToTerms;

    // Note: The matcher will feed up the same term value over repeatedly
    // for multiple docs before moving on to the next term value.  These
    // two fields allow us to reuse singletons for as long
    private Object previousTermValue = null;
    private List<String> currentSingleton = NULL_SINGLETON;

    @SuppressWarnings("unchecked")
    public DefaultMatchHandler(int maxdoc)
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
        List<String> currentValue = docToTerms[doc];
        if (currentValue == null)
        {
            docToTerms[doc] = currentSingleton;
        }
        else if (currentValue.size() == 1)
        {
            String previousTermValue = currentValue.get(0);
            currentValue = new ArrayList<String>(2);
            currentValue.add(previousTermValue);
            currentValue.add(termValue);
            docToTerms[doc] = currentValue;
        }
        else
        {
            currentValue.add(termValue);
        }
    }

    public List<String>[] getResults()
    {
        return docToTerms;
    }
}
