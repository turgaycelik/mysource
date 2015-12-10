package com.atlassian.jira.issue.customfields.searchers.information;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Stores information on Custom Field Searchers.
 *
 * @since v4.0
 */
public class CustomFieldSearcherInformation extends GenericSearcherInformation<CustomField>
{
    private final List<FieldIndexer> indexers;
    private final AtomicReference<CustomField> fieldReference;

    public CustomFieldSearcherInformation(final String id, final String nameKey, final List<? extends FieldIndexer> indexers,
            final AtomicReference<CustomField> fieldReference)
    {
        super(id, nameKey, Collections.<Class<? extends FieldIndexer>>emptyList(), fieldReference, SearcherGroupType.CUSTOM);
        this.indexers = CollectionUtil.copyAsImmutableList(Assertions.notNull("indexers", indexers));
        Assertions.stateTrue("indexers", !this.indexers.isEmpty());
        this.fieldReference = fieldReference;
    }

    /**
     * Regular {@link com.atlassian.jira.issue.search.searchers.IssueSearcher}s get their {@link FieldIndexer}s
     * by instantiating the class objects passed to them. However, Custom Fields work differently because they
     * have their indexers instantiated elsewhere for them.
     *
     * @return the indexers for this custom field searcher
     */
    @Override
    public List<FieldIndexer> getRelatedIndexers()
    {
        final CustomField customField = fieldReference.get();
        Assertions.stateNotNull("customField", customField);
        List<FieldIndexer> relatedIndexers = customField.getCustomFieldType().getRelatedIndexers(customField);
        if (relatedIndexers != null)
        {
            return relatedIndexers;
        }
        else
        {
            return indexers;
        }
    }

    @Override
    public String getNameKey()
    {
        return fieldReference.get().getNameKey();
    }
}
