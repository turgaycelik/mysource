package com.atlassian.jira.mock.issue.search.searchers.information;

import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.List;

/**
 * Simple mock implementation of {@link com.atlassian.jira.issue.search.searchers.information.SearcherInformation}.
 *
 * @since v4.0
 */
public class MockSearcherInformation<T extends SearchableField> implements SearcherInformation<T>
{
    private String id;
    private String nameKey;
    private String name;
    private T field;
    private List<FieldIndexer> indexers;
    private SearcherGroupType searcherGroupType;

    public MockSearcherInformation(final String id, final String nameKey, final String name, final T field,
            final List<FieldIndexer> indexers, final SearcherGroupType searcherGroup)
    {
        this.id = id;
        this.nameKey = nameKey;
        this.name = name;
        this.field = field;
        this.indexers = indexers;
        this.searcherGroupType = searcherGroup;
    }

    public MockSearcherInformation(final String name)
    {
        this(name, name, name, null, null, null);
    }

    public String getId()
    {
        return id;
    }

    public String getNameKey()
    {
        return nameKey;
    }

    public T getField()
    {
        return field;
    }

    public List<FieldIndexer> getRelatedIndexers()
    {
        return indexers;
    }

    public SearcherGroupType getSearcherGroupType()
    {
        return searcherGroupType;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public void setNameKey(final String nameKey)
    {
        this.nameKey = nameKey;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public void setField(final T field)
    {
        this.field = field;
    }

    public void setIndexers(final List<FieldIndexer> indexers)
    {
        this.indexers = indexers;
    }

    public void setSearcherGroupType(final SearcherGroupType searcherGroupType)
    {
        this.searcherGroupType = searcherGroupType;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
