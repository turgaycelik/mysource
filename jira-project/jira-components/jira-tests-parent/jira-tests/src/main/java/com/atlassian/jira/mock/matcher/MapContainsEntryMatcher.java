package com.atlassian.jira.mock.matcher;

import org.easymock.IArgumentMatcher;

import java.util.Map;

/**
 * Matcher for maps.
 *
 * @since v4.4
 */
public class MapContainsEntryMatcher<K, V> implements IArgumentMatcher
{
    public static <K, V> Map<K, V> containsEntry(K k, V v)
    {
        org.easymock.EasyMock.reportMatcher(new MapContainsEntryMatcher<K, V>(k, v));
        return null;
    }

    private final K expectedKey;
    private final V expectedValue;

    public MapContainsEntryMatcher(K expectedKey, V expectedValue)
    {
        this.expectedKey = expectedKey;
        this.expectedValue = expectedValue;
    }

    @Override
    public boolean matches(Object argument)
    {
        try
        {
            Map map = ((Map) argument);

            return map.containsKey(expectedKey) && expectedValue.equals(map.get(expectedKey));
        }
        catch (ClassCastException e)
        {
            return false;
        }
    }

    @Override
    public void appendTo(StringBuffer buffer)
    {
        buffer.append(String.format("Expected: a java.util.Map containing the entry (%s, %s)", expectedKey, expectedValue));
    }
}
