package com.atlassian.jira.external.beans;

/**
 * Used to represent a ChangeItem when importing data.
 *
 * @since v3.13
 */
public class ExternalChangeItem
{
    private final String id;
    private final String changeGroupId;
    private final String fieldType;
    private final String field;
    private final String oldValue;
    private final String oldString;
    private final String newValue;
    private final String newString;

    public ExternalChangeItem(final String id, final String changeGroupId, final String fieldType, final String field, final String oldValue, final String oldString, final String newValue, final String newString)
    {
        this.id = id;
        this.changeGroupId = changeGroupId;
        this.fieldType = fieldType;
        this.field = field;
        this.oldValue = oldValue;
        this.oldString = oldString;
        this.newValue = newValue;
        this.newString = newString;
    }

    public String getId()
    {
        return id;
    }

    public String getChangeGroupId()
    {
        return changeGroupId;
    }

    public String getFieldType()
    {
        return fieldType;
    }

    public String getField()
    {
        return field;
    }

    public String getOldValue()
    {
        return oldValue;
    }

    public String getOldString()
    {
        return oldString;
    }

    public String getNewValue()
    {
        return newValue;
    }

    public String getNewString()
    {
        return newString;
    }
}
