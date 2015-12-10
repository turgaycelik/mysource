package com.atlassian.jira.mock.issue.fields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.util.I18nHelper;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since v4.0
 */
public class MockNavigableField implements NavigableField, Comparable
{
    private final String id;

    public MockNavigableField(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public String getColumnHeadingKey()
    {
        return null;
    }

    public String getColumnCssClass()
    {
        return null;
    }

    public String getDefaultSortOrder()
    {
        return null;
    }

    public FieldComparatorSource getSortComparatorSource()
    {
        return null;
    }

    @Override
    public List<SortField> getSortFields(boolean sortOrder)
    {
        return Collections.emptyList();
    }

    public LuceneFieldSorter getSorter()
    {
        return null;
    }

    public String getColumnViewHtml(final FieldLayoutItem fieldLayoutItem, final Map displayParams, final Issue issue)
    {
        return null;
    }

    public String getHiddenFieldId()
    {
        return null;
    }

    public String prettyPrintChangeHistory(final String changeHistory)
    {
        return null;
    }

    public String prettyPrintChangeHistory(final String changeHistory, final I18nHelper i18nHelper)
    {
        return null;
    }

    public String getNameKey()
    {
        return null;
    }

    public String getName()
    {
        return null;
    }


    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final MockNavigableField that = (MockNavigableField) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }


    public int compareTo(final Object o)
    {
        return id.compareTo(((MockNavigableField)o).id);
    }
}
