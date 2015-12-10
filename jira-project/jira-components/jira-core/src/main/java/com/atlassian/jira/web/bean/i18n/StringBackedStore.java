package com.atlassian.jira.web.bean.i18n;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Uses String for both keys and values.
 *
 * @since 6.2
 */
public class StringBackedStore implements TranslationStore
{
    private final ImmutableMap<String, String> map;

    public StringBackedStore(Map<String, String> map)
    {
        this.map = ImmutableMap.copyOf(map);
    }

    public static StringBackedStore fromMap(Map<String, String> map) {
        return new StringBackedStore(map);
    }

    @Override
    public String get(final String key)
    {
        return map.get(key);
    }

    @Override
    public boolean containsKey(final String key)
    {
        return map.containsKey(key);
    }

    @Override
    public Iterable<String> keys()
    {
        return map.keySet();
    }
}
