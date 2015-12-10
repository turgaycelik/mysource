package com.atlassian.jira.issue.customfields;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.LuceneFieldSorter;

/**
 * This interface indicates the values stored in the Lucene index for this searcher are naturally
 * ordered and as a result natural Lucene sorting can be used allowing for best sort performance.
 *
 */
@PublicSpi
public interface NaturallyOrderedCustomFieldSearcher
{
    /**
     * Retrun the name of the Lucene field to use for ordering.  This will normally be just the field,
     * .i.e customField.getId() but some searchers, e.g. TextSearcher, store additional fields to support sorting.
     * @param customField The custom field to be searched
     * @return The Lucene field name to use for orderring.
     */
    public String getSortField(CustomField customField);
}
