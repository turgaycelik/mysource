package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.DefaultIPMatcher;
import com.atlassian.security.auth.trustedapps.DefaultURLMatcher;
import com.atlassian.security.auth.trustedapps.IPMatcher;
import com.atlassian.security.auth.trustedapps.URLMatcher;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public final class TrustedApplicationUtil
{
    static URLMatcher getURLMatcher(final String urlMatch)
    {
        return new DefaultURLMatcher(getLines(urlMatch));
    }

    static IPMatcher getIPMatcher(final String string)
    {
        return new DefaultIPMatcher(getLines(string));
    }

    public static Set<String> getLines(final String ipMatch)
    {
        if (StringUtils.isBlank(ipMatch))
        {
            return Collections.emptySet();
        }
        final BufferedReader reader = new BufferedReader(new StringReader(ipMatch));
        final Set<String> ipLines = new LinkedHashSet<String>();
        try
        {
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
                ipLines.add(line);
            }
        }
        ///CLOVER:OFF
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
        ///CLOVER:ON
        return ipLines;
    }

    static String getMultilineString(final Set<String> set)
    {
        if ((set == null) || set.isEmpty())
        {
            return "";
        }
        final Iterator<String> it = set.iterator();
        final StringBuilder result = new StringBuilder(it.next());
        while (it.hasNext())
        {
            result.append("\n");
            result.append(it.next());
        }
        return result.toString();
    }

    /**
     * Canonicalise a multi-line String to line-feed encoded.
     *
     * @param string to canonicalise
     * @return the canonised String
     */
    static String canonicalize(final String string)
    {
        if (string == null)
        {
            return null;
        }
        return getMultilineString(getLines(string));
    }

    /**
     * Canonicalise a Set of strings to a single line-feed encoded.
     *
     * @param urlPatterns to canonicalise
     * @return the canonised String
     */
    static String canonicalize(Iterable<String> urlPatterns)
    {
        return StringUtils.join(urlPatterns.iterator(), "\n");
    }

    ///CLOVER:OFF
    private TrustedApplicationUtil()
    {}

    ///CLOVER:ON
}