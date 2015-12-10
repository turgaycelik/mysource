package com.atlassian.jira.issue.search.searchers.information;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Provides name and index information for the resolution searcher.
 *
 * @since v4.0
 */
public class GenericSearcherInformation<T extends SearchableField> implements SearcherInformation<T>
{
    private final String id;
    private final String nameKey;
    private final List<Class<? extends FieldIndexer>> indexers;
    private final AtomicReference<T> fieldReference;
    private final SearcherGroupType searcherGroupType;
    
    public GenericSearcherInformation(final String id, final String nameKey, final List<Class<? extends FieldIndexer>> indexers,
            final AtomicReference<T> fieldReference, final SearcherGroupType searcherGroupType)
    {
        this.id = notBlank("id", id);
        this.nameKey = notBlank("nameKey", nameKey);
        this.indexers = Assertions.containsNoNulls("indexers", indexers);
        this.fieldReference = notNull("fieldReference", fieldReference);
        this.searcherGroupType = notNull("searcherGroupType", searcherGroupType);
    }

    public String getId()
    {
        return id;
    }

    public String getNameKey()
    {
        return nameKey;
    }

    public List<FieldIndexer> getRelatedIndexers()
    {
        final List<FieldIndexer> relatedIndexers = new ArrayList<FieldIndexer>();
        for (Class<? extends FieldIndexer> clazz : indexers)
        {
            relatedIndexers.add(loadIndexer(clazz));
        }
        return relatedIndexers;
    }

    public SearcherGroupType getSearcherGroupType()
    {
        return searcherGroupType;
    }

    public T getField()
    {
        return fieldReference.get();
    }

    FieldIndexer loadIndexer(Class<? extends FieldIndexer> clazz)
    {
        try
        {
            return ComponentAccessor.getComponent(ComponentClassManager.class).newInstance(clazz.getName());
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Failed to load indexer '" + clazz.getName() + "'", e);
        }
    }
}
