package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This column layout is used to be able to create columns that are just producing HTML.
 * <p/>
 * It works in conjunction with {@link IssueTableWebComponent}
 */
public abstract class SimpleColumnLayoutItem implements ColumnLayoutItem
{
    private SimpleNavigableField simpleNavigableField = new SimpleNavigableField();

    public NavigableField getNavigableField()
    {
        return simpleNavigableField;
    }

    @Override
    public String getId()
    {
        return getNavigableField().getId();
    }

    public int getPosition()
    {
        throw new UnsupportedOperationException("getPosition not implemented");
    }

    /**
     * Subclasses need to implement this to return the HTML for each row.
     */
    public abstract String getHtml(Map displayParams, Issue issue);

    /**
     * Subclasses can override this to provide their own CSS class if they need to
     * @return a css class - defaults to empty String
     */
    protected String getColumnCssClass()
    {
        return "";
    }

    public String getColumnHeadingKey()
    {
        return getNavigableField().getColumnHeadingKey();
    }

    @Override
    public boolean isAliasForField(final User user, final String sortField)
    {
        return false;
    }

    @Override
    public boolean isAliasForField(ApplicationUser user, String sortField)
    {
        return false;
    }

    /**
     * Subclasses can override this method to provide specific header html
     *
     */
    public String getHeaderHtml()
    {
        return null;
    }

    public int compareTo(Object o)
    {
        throw new UnsupportedOperationException("compareTo not implemented");
    }

    /**
     * @deprecated
     */
    protected Issue getIssueObject(GenericValue genericValue)
    {
        return IssueImpl.getIssueObject(genericValue);
    }

    private class SimpleNavigableField implements NavigableField
    {
        public String getColumnHeadingKey()
        {
            return "";
        }

        public String getColumnCssClass()
        {
            return SimpleColumnLayoutItem.this.getColumnCssClass();
        }

        public String getDefaultSortOrder()
        {
            throw new UnsupportedOperationException("getDefaultSortOrder not implemented");
        }

        public LuceneFieldSorter getSorter()
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

        public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
        {
            throw new UnsupportedOperationException("getColumnViewHtml not implemented");
        }

        public String getHiddenFieldId()
        {
            return null;
        }

        public String getId()
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

        public int compareTo(Object o)
        {
            return 0;
        }

        public String prettyPrintChangeHistory(String changeHistory)
        {
            return changeHistory;
        }

        public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
        {
            return changeHistory;
        }
    }
}
