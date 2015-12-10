package com.atlassian.jira.matchers;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

import java.util.Map;

/**
 * Matchers for {@link java.util.Map}s.
 *
 * @since v5.2
 */
public final class MapMatchers
{

    private MapMatchers()
    {
        throw new AssertionError("Don't instantiate me");
    }


    public static <K,V> Matcher<Map<K,V>> isSingletonMap(final K expectedKey, final V expectedValue)
    {
        return new TypeSafeMatcher<Map<K, V>>()
        {
            @Override
            protected boolean matchesSafely(Map<K, V> map)
            {
                return map.size() == 1 && map.containsKey(expectedKey) && map.containsValue(expectedValue);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("a map with exactly one entry: [").appendValue(expectedKey)
                        .appendText(", ").appendValue(expectedValue).appendText("]");

            }
        };
    }

    /**
     * Guava factory style map matcher for a map containing exactly 2 entries matching the arguments.
     *
     * @param key1 key of the first entry
     * @param value1 value of the first entry
     * @param key2 key of the second entry
     * @param value2 key of the second entry
     * @param <K> key type
     * @param <V> value type
     * @return matcher for the map equal to a map of provided keys and values
     */
    public static <K,V> Matcher<Map<K,V>> isMapOf(final K key1, final V value1, final K key2, final V value2)
    {
        // HAMCREST 1.2, Y U USE STUPID GENERIC BOUNDS :/
        return (Matcher<Map<K,V>>)Matchers.equalTo((Map<K,V>)ImmutableMap.of(key1, value1, key2, value2));
    }
}
