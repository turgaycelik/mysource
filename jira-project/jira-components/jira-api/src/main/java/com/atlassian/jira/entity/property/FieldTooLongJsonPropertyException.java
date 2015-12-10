package com.atlassian.jira.entity.property;

/**
 * Indicates that one of the data fields provided for a JSON property was too long.
 *
 * @since v6.1
 */
public class FieldTooLongJsonPropertyException extends IllegalArgumentException
{
    private final String field;
    private final int actualLength;
    private final int maximumLength;

    public FieldTooLongJsonPropertyException(String field, int actualLength, int maximumLength)
    {
        super("Value specified for field '" + field + "' is too long: " + actualLength + " > " + maximumLength);
        this.field = field;
        this.actualLength = actualLength;
        this.maximumLength = maximumLength;
    }

    public String getField()
    {
        return field;
    }

    public int getActualLength()
    {
        return actualLength;
    }

    public int getMaximumLength()
    {
        return maximumLength;
    }
}
