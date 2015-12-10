package com.atlassian.jira.mock.issue.fields;

import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.search.SearchHandler;

/**
 * Simple mock searchable field for testsing.
 *
 * @since v4.0
 */
public class MockSearchableField implements SearchableField
{
    private SearchHandler searchHandler;
    private String id;
    private String nameKey;
    private String name;

    public MockSearchableField(final String id, final String name, final String nameKey, final SearchHandler searchHandler)
    {
        this.searchHandler = searchHandler;
        this.id = id;
        this.nameKey = nameKey;
        this.name = name;
    }

    public MockSearchableField(final String id)
    {
        this(id, id, id, null);
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        return searchHandler;
    }

    public String getId()
    {
        return id;
    }

    public String getNameKey()
    {
        return nameKey;
    }

    public String getName()
    {
        return name;
    }

    public int compareTo(final Object o)
    {
        throw new UnsupportedOperationException();
    }

    public void setSearchHandler(final SearchHandler searchHandler)
    {
        this.searchHandler = searchHandler;
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
}
