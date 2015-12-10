package com.atlassian.jira.web.component;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class contains a collection of utility methods used by web components 
 *
 * @since v3.12.4
 */
public class WebComponentUtils
{

    /**
     * Converts a string of comma-separated values to a collection of those values
     *
     * @param s string to convert
     * @return a collection of String values, never null
     */
    public static Collection<String> convertStringToCollection(final String s)
    {
        final Collection<String> collection = new ArrayList<String>();
        if (!StringUtils.isBlank(s))
        {
            for (final StringTokenizer tokenizer = new StringTokenizer(s, ","); tokenizer.hasMoreTokens();)
            {
                final String value = tokenizer.nextToken().trim();
                if (StringUtils.isNotBlank(value))
                {
                    collection.add(value);
                }
            }
        }
        return collection;
    }

    /**
     * Picks up all keys that start with paramPrefix.
     * <p/>
     * For example, when params map contains a 'removegroups_jira-users' key, the returned collection
     * will contain 'jira-users' string.
     *
     * @param params      map of parameters - name-value pairs
     * @param paramPrefix parameter name prefix
     * @return a collection of
     */
    public static Collection<String> getRemovalValues(final Map<String, ?> params, final String paramPrefix)
    {
        final Collection<String> collection = new ArrayList<String>();
        if (params != null)
        {
            for (final String key : params.keySet())
            {
                if (key.startsWith(paramPrefix))
                {
                    collection.add(key.substring(paramPrefix.length()));
                }
            }
        }
        return collection;
    }

}
