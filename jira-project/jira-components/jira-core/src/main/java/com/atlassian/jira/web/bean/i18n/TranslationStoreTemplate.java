package com.atlassian.jira.web.bean.i18n;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Iterator;
import java.util.Map;

/**
 * Templates class for TranslationStore implementations. Subclasses need only implement #makeKeyFromString and #makeValueFromString.
 *
 * @param <K> type parameter for the key
 * @param <V> type parameter for the value
 */
public abstract class TranslationStoreTemplate<K, V> implements TranslationStore
{
    private final ImmutableMap<K, V> map;

    public TranslationStoreTemplate(Map<String, String> map)
    {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (Map.Entry<String, String> entry : map.entrySet())
        {
            builder.put(makeKeyFromString(entry.getKey()), makeValueFromString(entry.getValue()));
        }

        this.map = builder.build();
    }

    @Override
    public String get(String key)
    {
        V value = map.get(makeKeyFromString(key));

        return value != null ? value.toString() : null;
    }

    @Override
    public boolean containsKey(String key)
    {
        return map.containsKey(makeKeyFromString(key));
    }

    @Override
    public Iterable<String> keys()
    {
        return new Iterable<String>()
        {
            @Override
            public Iterator<String> iterator()
            {
                return new KeysIterator();
            }
        };
    }

    protected abstract K makeKeyFromString(String key);

    protected abstract V makeValueFromString(String value);

    private class KeysIterator implements Iterator<String>
    {
        private final UnmodifiableIterator<K> it;

        private KeysIterator()
        {
            this.it = map.keySet().iterator();
        }

        @Override
        public boolean hasNext()
        {
            return it.hasNext();
        }

        @Override
        public String next()
        {
            K next = it.next();
            return next != null ? next.toString() : null;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
