package com.atlassian.jira.issue;

/**
 * Struct-like class for storing an old field value/new field value pair.
 */
public class ModifiedValue
{
    private final Object oldValue;
    private final Object newValue;

    /**
     * Construct a field modification pair. Objects are field-specific,
     * eg. from {@link com.atlassian.jira.issue.fields.CustomField#getValue}
     * @param oldValue the old value
     * @param newValue the new value
     */
    public ModifiedValue(Object oldValue, Object newValue)
    {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Object getOldValue()
    {
        return oldValue;
    }

    public Object getNewValue()
    {
        return newValue;
    }
}
