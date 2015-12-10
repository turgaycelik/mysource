package com.atlassian.jira.util.lang;

import javax.annotation.Nullable;

/**
 * JIRA String utilitites.
 *
 * @since v4.2
 */
public final class JiraStringUtils
{
    public static final int EXPECTED_ELEMENT_LENGTH = 8;

    private JiraStringUtils()
    {
    }

    /**
     * Concatenate array of objects into a string in accordance with
     * JLS $15.18.1 (except that primitive values are not accepted
     * by this method other than by autoboxing to primitive wrappers).
     *
     * @param elements elements to convert
     * @return string resulting from concatenating <tt>elements</tt>
     */
    public static String asString(@Nullable Object... elements)
    {
        // don't rename to toString, its not usable for static imports 
        int length = elements.length;
        if (length  == 0)
        {
            return "";
        }
        if (length == 1)
        {
            asString(elements[0]);
        }
        StringBuilder answer = new StringBuilder(length * EXPECTED_ELEMENT_LENGTH);
        for (Object elem : elements)
        {
            answer.append(asString(elem));
        }
        return answer.toString();
    }

    private static String asString(Object obj)
    {
        return obj != null ? obj.toString() : "null";
    }
}
