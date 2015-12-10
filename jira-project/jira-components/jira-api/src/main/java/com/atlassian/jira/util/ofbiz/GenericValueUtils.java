package com.atlassian.jira.util.ofbiz;

import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.atlassian.annotations.Internal;

/**
 * Only included in APi because it was required by some implementations in order to compile.
 *
 * @deprecated Use API methods that don't require GenericValues instead, or copy/paste these methods as we intend to pull this out of the API. Since v6.1.
 */
@Internal
public class GenericValueUtils
{
    public static Long[] transformToLongIds(final Collection<GenericValue> genericValues)
    {
        if (genericValues != null)
        {
            final Long[] ids = new Long[genericValues.size()];
            int i = 0;
            for (final GenericValue gv : genericValues)
            {
                if (gv != null)
                {
                    ids[i] = gv.getLong("id");
                }
                else
                {
                    ids[i] = new Long(-1);
                }
                i++;
            }
            return ids;
        }
        else
        {
            return null;
        }
    }

    public static List<Long> transformToLongIdsList(final Collection<GenericValue> genericValues)
    {
        if (genericValues == null)
        {
            // For consistency with transformToLongIds(): the alternative was a NullPointerException
            return null;
        }
        return Arrays.asList(transformToLongIds(genericValues));
    }

    public static String[] transformToStrings(final Collection<GenericValue> genericValues, final String fieldName)
    {
        if ((genericValues != null) && (fieldName != null))
        {
            final String[] strings = new String[genericValues.size()];
            int i = 0;
            for (final GenericValue gv : genericValues)
            {
                if (gv != null)
                {
                    strings[i] = gv.getString(fieldName);
                }
                else
                {
                    strings[i] = "-1";
                }
                i++;
            }
            return strings;
        }
        else
        {
            return null;
        }
    }

    public static String[] transformToStringIds(final Collection<GenericValue> genericValues)
    {
        if (genericValues != null)
        {
            final String[] ids = new String[genericValues.size()];
            int i = 0;
            for (final GenericValue gv : genericValues)
            {
                if (gv != null)
                {
                    ids[i] = gv.getString("id");
                }
                else
                {
                    ids[i] = "-1";
                }
                i++;
            }
            return ids;
        }
        else
        {
            return null;
        }
    }

    public static List<String> transformToStringIdsList(final Collection<GenericValue> genericValues)
    {
        if (genericValues == null)
        {
            // For consistency with transformToStringIds()
            return null;
        }
        return Arrays.asList(transformToStringIds(genericValues));
    }

    /**
     * Returns a String containing a comma and space (for display)
     * separated list of the key property values in the given collection of GenericValues.
     *
     * @param genericValues Collection of {@link org.ofbiz.core.entity.GenericValue} objects.
     * @param key           We pull this property out of each GV for our return values.
     * @return a String containing a comma separated list of the key property values in the given collection of GenericValues.
     * @since v3.13
     */
    public static String getCommaSeparatedList(final Collection<GenericValue> genericValues, final String key)
    {
        if (genericValues == null)
        {
            return null;
        }
        if (StringUtils.isEmpty(key))
        {
            throw new IllegalArgumentException("key must be a non-empty String");
        }
        final StringBuilder sb = new StringBuilder();
        for (final Iterator<GenericValue> iterator = genericValues.iterator(); iterator.hasNext();)
        {
            final GenericValue gv = iterator.next();
            sb.append(gv.getString(key));
            if (iterator.hasNext())
            {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
