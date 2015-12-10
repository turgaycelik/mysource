package com.atlassian.jira.imports.project.customfield;

/**
 * Used to store a custom field value for use of importing a backup JIRA project into an existing JIRA instance.
 *
 * @since v3.13
 */
public class ExternalCustomFieldValueImpl implements ExternalCustomFieldValue
{
    private final String id;
    private final String customFieldId;
    private final String issueId;
    private String parentKey;
    private String stringValue;
    private String numberValue;
    private String textValue;
    private String dateValue;

    public ExternalCustomFieldValueImpl(final String id, final String customFieldId, final String issueId)
    {
        this.customFieldId = customFieldId;
        this.id = id;
        this.issueId = issueId;
    }

    public String getValue()
    {
        if (stringValue != null)
        {
            return stringValue;
        }
        if (numberValue != null)
        {
            return numberValue;
        }
        if (textValue != null)
        {
            return textValue;
        }
        if (dateValue != null)
        {
            return dateValue;
        }
        return null;
    }

    public String getCustomFieldId()
    {
        return customFieldId;
    }

    @Override
    public String getDateValue()
    {
        return dateValue;
    }

    public void setDateValue(final String dateValue)
    {
        this.dateValue = dateValue;
    }

    public String getId()
    {
        return id;
    }

    public String getIssueId()
    {
        return issueId;
    }

    @Override
    public String getNumberValue()
    {
        return numberValue;
    }

    public void setNumberValue(final String numberValue)
    {
        this.numberValue = numberValue;
    }

    public String getParentKey()
    {
        return parentKey;
    }

    public void setParentKey(final String parentKey)
    {
        this.parentKey = parentKey;
    }

    @Override
    public String getStringValue()
    {
        return stringValue;
    }

    public void setStringValue(final String stringValue)
    {
        this.stringValue = stringValue;
    }

    @Override
    public String getTextValue()
    {
        return textValue;
    }

    public void setTextValue(final String textValue)
    {
        this.textValue = textValue;
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

        final ExternalCustomFieldValueImpl that = (ExternalCustomFieldValueImpl) o;

        if (customFieldId != null ? !customFieldId.equals(that.customFieldId) : that.customFieldId != null)
        {
            return false;
        }
        if (dateValue != null ? !dateValue.equals(that.dateValue) : that.dateValue != null)
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
        if (numberValue != null ? !numberValue.equals(that.numberValue) : that.numberValue != null)
        {
            return false;
        }
        if (parentKey != null ? !parentKey.equals(that.parentKey) : that.parentKey != null)
        {
            return false;
        }
        if (stringValue != null ? !stringValue.equals(that.stringValue) : that.stringValue != null)
        {
            return false;
        }
        if (textValue != null ? !textValue.equals(that.textValue) : that.textValue != null)
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
        result = 31 * result + (issueId != null ? issueId.hashCode() : 0);
        result = 31 * result + (parentKey != null ? parentKey.hashCode() : 0);
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        result = 31 * result + (numberValue != null ? numberValue.hashCode() : 0);
        result = 31 * result + (textValue != null ? textValue.hashCode() : 0);
        result = 31 * result + (dateValue != null ? dateValue.hashCode() : 0);
        return result;
    }
}
