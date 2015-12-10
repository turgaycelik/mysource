package com.atlassian.jira.util;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

import java.util.Map;
import java.util.Set;

/**
 * Utility methods that work on Maps.
 *
 * @since v4.3
 *
 * @deprecated Use Guava or build your own inverter instead. Since v6.3.
 */
public class MapUtils
{
    public static MultiMap invertMap(final Map mapToInvert)
    {
        final MultiMap invertedMap = new MultiHashMap();
        final Set entries = mapToInvert.entrySet();
        for (final Object entry1 : entries)
        {
            final Map.Entry entry = (Map.Entry) entry1;
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            invertedMap.put(value, key);
        }
        return invertedMap;
    }
}
