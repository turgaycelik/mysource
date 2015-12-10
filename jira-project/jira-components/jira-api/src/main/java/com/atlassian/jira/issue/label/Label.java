package com.atlassian.jira.issue.label;

import com.atlassian.annotations.PublicApi;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents a Label for an issue
 *
 * @since v4.2
 */
@PublicApi
public final class Label
{
    private final Long id;
    private final Long issue;
    private final Long customFieldId;
    private final String label;

    public Label(final Long id, final Long issue, final String label)
    {
        this(id, issue, null, label);
    }

    public Label(final Long id, final Long issue, final Long customFieldId, final String label)
    {
        this.id = id;
        this.issue = issue;
        this.label = notNull("label", label);
        this.customFieldId = customFieldId;
    }

    public Long getCustomFieldId()
    {
        return customFieldId;
    }

    public Long getId()
    {
        return id;
    }

    public Long getIssue()
    {
        return issue;
    }

    public String getLabel()
    {
        return label;
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

        final Label label1 = (Label) o;

        if (customFieldId != null ? !customFieldId.equals(label1.customFieldId) : label1.customFieldId != null)
        {
            return false;
        }
        if (id != null ? !id.equals(label1.id) : label1.id != null)
        {
            return false;
        }
        if (issue != null ? !issue.equals(label1.issue): label1.issue != null)
        {
            return false;
        }
        if (!label.equals(label1.label))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (issue != null ? issue.hashCode() : 0);
        result = 31 * result + (customFieldId != null ? customFieldId.hashCode() : 0);
        result = 31 * result + label.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
