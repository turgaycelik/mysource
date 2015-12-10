package com.atlassian.jira.web.bean.i18n;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @since v6.2.3
 */
public class MockTranslationStore implements TranslationStore
{
    private final Map<String, String> store;

    public MockTranslationStore(String...pairs)
    {
        final Map<String, String> store = Maps.newHashMap();
        putFromArray(pairs, store);
        this.store = store;
    }

    public MockTranslationStore(Map<String, String> pairs)
    {
        store = Maps.newHashMap(pairs);
    }

    public Map<String, String> asMap()
    {
        return store;
    }

    @Override
    public String get(final String key)
    {
        return store.get(key);
    }

    @Override
    public boolean containsKey(final String key)
    {
        return store.containsKey(key);
    }

    @Override
    public Iterable<String> keys()
    {
        return store.keySet();
    }

    public MockTranslationStore add(String...pairs)
    {
        putFromArray(pairs, store);
        return this;
    }

    public MockTranslationStore add(String key, String value)
    {
        store.put(key, value);
        return this;
    }

    private static void putFromArray(final String[] pairs, final Map<String, String> data)
    {
        if ((pairs.length & 0x1) == 1)
        {
            throw new IllegalArgumentException("Need a even number of pairs.");
        }

        for (int i = 0; i < pairs.length;)
        {
            String key = pairs[i++];
            String value = pairs[i++];
            data.put(key, value);
        }
    }
}
