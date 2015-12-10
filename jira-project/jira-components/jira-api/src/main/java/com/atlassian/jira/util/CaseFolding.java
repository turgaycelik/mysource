package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;

import java.util.Locale;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Utility for case folding Java Strings. This follows the advice given <a
 * href="http://www.w3.org/International/wiki/Case_folding">here</a>. Basically the value is case folded by calling
 * {@code String.toUpperCase(locale).toLowerCase(locale)}. This is a little better than calling only {@code
 * String.toLowerCase(locale)}. For example, in German the string "&#x00df;" upercase equilavent is "SS". If we only
 * called only {@link String#toLowerCase(java.util.Locale)} we would store "&#x00df;"in the index which would make
 * matching "SS" impossible. This does not always case fold correctly, for example the Turkish 'I', but it is a good
 * compromise.
 *
 * @since v4.0
 */
public class CaseFolding
{
    /**
     * Case fold the passed string using the ENGLISH locale.
     *
     * @param s1 the string to case fold. Cannot be null.
     * @return the case folded string.
     */
    public static String foldString(String s1)
    {
        return foldString(s1, Locale.ENGLISH);
    }

    /**
     * Case fold the passed username using the method defined by Crowd Embedded.
     * Will return null output for null input.
     *
     * @param username the username to case fold.
     * @return the case folded username.
     * @deprecated Use {@link com.atlassian.jira.user.ApplicationUser#getKey()} or {@link IdentifierUtils#toLowerCase(String)}. Since v6.0
     */
    public static String foldUsername(String username)
    {
        if (username == null)
        {
            return null;
        }
        return IdentifierUtils.toLowerCase(username);
    }

    /**
     * Case fold the passed string using the passed locale. You probaly should not use this method because setting
     * a locale may result in non-standard case folding. For example, using the Turkish locale will case fold "I" into
     * "&#x0131;" (dotless lowercase letter i).
     *
     * @param s1 the string to case fold. Cannot be null.
     * @param locale the locale to use for folding. Cannot be null.
     * @return the case folded string.
     */
    public static String foldString(String s1, Locale locale)
    {
        notNull("s1", s1);
        notNull("locale", locale);

        return s1.toUpperCase(locale).toLowerCase(locale);
    }
}
