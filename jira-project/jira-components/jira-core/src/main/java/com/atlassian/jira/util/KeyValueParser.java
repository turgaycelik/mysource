package com.atlassian.jira.util;

/**
 * Parses a key-value pair in a String such as "portNumber=5432" into its separate values
 *
 * @since v4.4
 */
public class KeyValueParser
{
    /**
     * Parses the input text into a key and value using '=' as a separator.
     * <p>
     *     eg "colour=red" will parse into "colour", "red"
     * <p>
     * If more than one equals sign is discovered, then the first is interpreted as the separator and subsequent ones are
     * interpreted as part of the "value".
     *
     * @param text The text to parse.
     * @return The parsed value
     */
    public static KeyValuePair<String, String> parse(String text)
    {
        if (text == null)
        {
            return null;
        }
        final int i = text.indexOf('=');
        if (i == -1)
        {
            // not found
            throw new IllegalArgumentException("No '=' found in '" + text + "'");
        }
        return new KeyValuePairImpl<String, String>(text.substring(0, i), text.substring(i + 1));
    }
}
