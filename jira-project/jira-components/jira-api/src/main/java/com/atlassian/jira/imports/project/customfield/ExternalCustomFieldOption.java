package com.atlassian.jira.imports.project.customfield;

/**
 * Stores information from a Custom Field Option.
 *
 * @since v3.13
 */
public class ExternalCustomFieldOption
{
    private final String id;
    private final String customFieldId;
    private final String fieldConfigId;
    private final String parentId;
    private final String value;

    public ExternalCustomFieldOption(final String id, final String customFieldId, final String fieldConfigId, final String parentId, final String value)
    {
        this.id = id;
        this.customFieldId = customFieldId;
        this.fieldConfigId = fieldConfigId;
        this.parentId = parentId;
        this.value = value;
    }

    public String getId()
    {
        return id;
    }

    public String getCustomFieldId()
    {
        return customFieldId;
    }

    public String getFieldConfigId()
    {
        return fieldConfigId;
    }

    public String getParentId()
    {
        return parentId;
    }

    public String getValue()
    {
        return value;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final ExternalCustomFieldOption that = (ExternalCustomFieldOption) o;

        if (customFieldId != null ? !customFieldId.equals(that.customFieldId) : that.customFieldId != null)
        {
            return false;
        }
        if (fieldConfigId != null ? !fieldConfigId.equals(that.fieldConfigId) : that.fieldConfigId != null)
        {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }
        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null)
        {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (id != null ? id.hashCode() : 0);
        result = 31 * result + (customFieldId != null ? customFieldId.hashCode() : 0);
        result = 31 * result + (fieldConfigId != null ? fieldConfigId.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
