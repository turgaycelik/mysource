package com.atlassian.jira.functest.framework.changehistory;

/**
 * A simple representation of a chanhe history field value, with old and new values
 *
 * @since v3.13
 */
public class ChangeHistoryField
{
    private final ChangeHistorySet set;
    private final String fieldName;
    private final String oldValue;
    private final String newValue;


    public ChangeHistoryField(ChangeHistorySet set, String fieldName, String oldValue, String newValue)
    {
        this.set = set;
        this.fieldName = canonical(fieldName);
        this.oldValue = canonical(oldValue);
        this.newValue = canonical(newValue);
    }

    static String canonical(String s)
    {
        return (s == null ? "" : s.trim());
    }


    public ChangeHistorySet getEntry()
    {
        return set;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public String getOldValue()
    {
        return oldValue;
    }

    public String getNewValue()
    {
        return newValue;
    }


    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ChangeHistoryField that = (ChangeHistoryField) o;

        if (fieldName != null ? !fieldName.equals(that.fieldName) : that.fieldName != null)
        {
            return false;
        }
        if (newValue != null ? !newValue.equals(that.newValue) : that.newValue != null)
        {
            return false;
        }
        if (oldValue != null ? !oldValue.equals(that.oldValue) : that.oldValue != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (fieldName != null ? fieldName.hashCode() : 0);
        result = 31 * result + (oldValue != null ? oldValue.hashCode() : 0);
        result = 31 * result + (newValue != null ? newValue.hashCode() : 0);
        return result;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\t'");
        sb.append(fieldName);
        sb.append("' ");
        sb.append("o: '");
        sb.append(oldValue);
        sb.append("'  ");
        sb.append("n: '");
        sb.append(newValue);
        sb.append("'");
        return sb.toString();
    }
}
