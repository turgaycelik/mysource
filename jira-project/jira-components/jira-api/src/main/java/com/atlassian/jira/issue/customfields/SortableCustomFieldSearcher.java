package com.atlassian.jira.issue.customfields;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.LuceneFieldSorter;

/**
 * A custom field searcher will implement this interface if the custom field can be sorted.  If a custom field
 * wishes to sort itself, it can use the slower method {@link com.atlassian.jira.issue.customfields.SortableCustomField}.
 *
 * @see com.atlassian.jira.issue.customfields.SortableCustomField
 * @see NaturallyOrderedCustomFieldSearcher
 */
@PublicSpi
public interface SortableCustomFieldSearcher
{
    public LuceneFieldSorter getSorter(CustomField customField);
}
