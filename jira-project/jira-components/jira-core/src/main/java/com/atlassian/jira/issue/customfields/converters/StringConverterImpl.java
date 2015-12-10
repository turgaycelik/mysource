package com.atlassian.jira.issue.customfields.converters;

public class StringConverterImpl implements StringConverter
{
    public String getString(String value)
    {
        // TODO: Is it the right thing to turn nulls into ""? this is inconsistent in Different Converters.
        // Some Converters do it (eg UserConverter) and others return null (eg DoubleConverter)
        return convertNullToEmpty(value);
    }

    public String getObject(String stringValue)
    {
        return stringValue;
    }

    /**
     * Returns Empty String if the passed value is null, else returns the original value.
     * @param value The String value to check for null.
     * @return Empty String if the passed value is null, else returns the original value.
     */
    public static String convertNullToEmpty(String value)
    {
        if (value == null)
            return "";
        return value;
    }
}
