package com.atlassian.jira.external.beans;

/**
 * Used to represent a label when importing data.
 *
 * @since v4.2
 */
public class ExternalLabel
{
    public String id;
    public String issueId;
    public String customFieldId;
    public String label;

    public String getCustomFieldId()
    {
        return customFieldId;
    }

    public void setCustomFieldId(final String customFieldId)
    {
        this.customFieldId = customFieldId;
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getIssueId()
    {
        return issueId;
    }

    public void setIssueId(final String issueId)
    {
        this.issueId = issueId;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(final String label)
    {
        this.label = label;
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

        final ExternalLabel that = (ExternalLabel) o;

        if (customFieldId != null ? !customFieldId.equals(that.customFieldId) : that.customFieldId != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (issueId != null ? !issueId.equals(that.issueId) : that.issueId != null)
        {
            return false;
        }
        if (label != null ? !label.equals(that.label) : that.label != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (issueId != null ? issueId.hashCode() : 0);
        result = 31 * result + (customFieldId != null ? customFieldId.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }
}
