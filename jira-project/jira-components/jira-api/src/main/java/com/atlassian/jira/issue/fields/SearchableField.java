package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.search.SearchHandler;

@PublicApi
public interface SearchableField extends Field
{
    /**
     * Return {@link SearchHandler} for the field. This object tells JIRA how to search for values within the field.
     *
     * @return the SearchHandler associated with the field. Can return <code>null</code> when no searcher
     * is associated with the field. This will mainly happen when a customfield is configured to have no
     * searcher.
     */
    public SearchHandler createAssociatedSearchHandler();
}
