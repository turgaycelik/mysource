package com.atlassian.jira.util;

import java.util.regex.Pattern;

/**
 * Contains static utility methods pertaining to colour values.
 *
 * @since v5.2
 */
public class Colours
{
    private static final Pattern hexColourPattern = Pattern.compile("#[\\da-fA-F]{3,6}");

    /**
     * Determines whether the input string is a valid hex encoded colour value.
     *
     * <p>See <a href="http://www.w3.org/TR/css3-color/#numerical"> CSS Color Module</a> for the details on how a valid
     * value is determined.</p>
     *
     * @param input The input string to be tested.
     * @return {@code true} if the input string is a valid hex encoded colour value. Otherwise, {@code false}.
     */
    public static boolean isHexColour(final String input)
    {
        return input != null && hexColourPattern.matcher(input).matches();
    }
}
