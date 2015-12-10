package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract base class for searchers that performs the init methods for a searcher.
 *
 * @since v4.0
 */
public abstract class AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    protected final AtomicReference<SearchableField> fieldReference;

    protected AbstractInitializationSearcher()
    {
        fieldReference = new AtomicReference<SearchableField>(null);
    }

    public void init(SearchableField field)
    {
        fieldReference.set(field);
    }
}
