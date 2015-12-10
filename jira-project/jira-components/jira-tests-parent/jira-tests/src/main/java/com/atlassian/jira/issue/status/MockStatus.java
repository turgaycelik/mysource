package com.atlassian.jira.issue.status;

import com.atlassian.jira.issue.MockIssueConstant;
import com.atlassian.jira.issue.status.category.StatusCategory;
import com.atlassian.jira.util.I18nHelper;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v3.13
 */
public class MockStatus extends MockIssueConstant implements Status
{

    private StatusCategory statusCategory;

    public MockStatus(final String id, final String name)
    {
        this(id, name, null);
    }

    public MockStatus(final String id, final String name, StatusCategory statusCategory)
    {
        super(id, name);
        this.statusCategory = statusCategory;
    }

    public MockStatus(final GenericValue gv, final StatusCategory statusCategory)
    {
        super(gv);
        this.statusCategory = statusCategory;
    }

    @Override
    public StatusCategory getStatusCategory()
    {
        return statusCategory;
    }

    @Override
    public void setStatusCategory(final StatusCategory statusCategory)
    {
        this.statusCategory = statusCategory;
    }

    @Override
    public SimpleStatus getSimpleStatus()
    {
        return new MockSimpleStatus(getId(), getName(), getDescription(), statusCategory, getIconUrl());
    }

    @Override
    public SimpleStatus getSimpleStatus(final I18nHelper i18nHelper)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        if (!super.equals(o)) { return false; }

        final MockStatus that = (MockStatus) o;

        if (statusCategory != null ? !statusCategory.equals(that.statusCategory) : that.statusCategory != null)
        { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (statusCategory != null ? statusCategory.hashCode() : 0);
        return result;
    }
}
