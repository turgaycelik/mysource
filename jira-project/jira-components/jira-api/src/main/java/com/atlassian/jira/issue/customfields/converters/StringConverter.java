package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;

/**
 * Converts Strings for storage and retrieval of Custom Field values.
 * This really doesn't do much at all because we already have Strings, but is included for consistency with the other Converters.
 */
@Internal
public interface StringConverter
{
    /**
     * Just turns null into an empty string.
     * 
     * @param value The String value to check for null.
     * @return Empty String if the passed value is null, else returns the original value.
     */
    public String getString(String value);

    /**
     * This method is a no-op, but I guess it was included for consistency with other Converters.
     * @param stringValue the stringValue to do absolutely nothing with, and just return as is.
     * @return The exact same object that you gave me.
     */
    public String getObject(String stringValue);
}
