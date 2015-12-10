package com.atlassian.jira.util.json;

/**
 * Escape util for JSON data
 *
 * @since v3.13
 */
public class JSONEscaper
{
    /**
     * This will escape a value ready to go into a JSON string value.
     * <p/>
     * NOTE : IT DOES NOT put the double quotes " around the string value and hence you
     * can safely append the returned value to another JSON string value
     * <p/>
     * This handles the use case where you are concattenating values that ultimately will be
     * a JSON string value but you dont want double quotes around too early.
     *
     * @param jsonStringValue JSON string value
     * @return an escaped JSON string value, returns empty string if the value is null
     */
    public static String escape(final String jsonStringValue)
    {
        final String quotedValue = JSONObject.quote(jsonStringValue);
        return quotedValue.substring(1, quotedValue.length() - 1);
    }
}
