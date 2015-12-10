package com.atlassian.jira.issue.status;

import com.atlassian.jira.issue.status.category.StatusCategory;

/**
 *
 * @since v6.1
 */
public class MockSimpleStatus implements SimpleStatus
{
    private String id;
    private String name;
    private String description;
    private StatusCategory statusCategory;
    private String iconUrl;

    public MockSimpleStatus(final String id, final String name, final String description, final StatusCategory statusCategory, final String iconUrl)
    {
        this.id = id;
        this.name = name;
        this.description = description;
        this.statusCategory = statusCategory;
        this.iconUrl = iconUrl;
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public StatusCategory getStatusCategory()
    {
        return statusCategory;
    }

    public void setStatusCategory(final StatusCategory statusCategory)
    {
        this.statusCategory = statusCategory;
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public void setIconUrl(final String iconUrl)
    {
        this.iconUrl = iconUrl;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final MockSimpleStatus that = (MockSimpleStatus) o;

        if (description != null ? !description.equals(that.description) : that.description != null) { return false; }
        if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null) { return false; }
        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
        if (statusCategory != null ? !statusCategory.equals(that.statusCategory) : that.statusCategory != null)
        { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (statusCategory != null ? statusCategory.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        return result;
    }
}
