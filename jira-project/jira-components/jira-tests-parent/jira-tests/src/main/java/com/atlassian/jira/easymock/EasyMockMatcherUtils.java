package com.atlassian.jira.easymock;

import org.easymock.EasyMock;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.util.dbc.Assertions;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Static utilities for easier creation of {@link org.easymock.classextension.EasyMock}
 * matchers.
 *
 * @since v4.2
 */
public final class EasyMockMatcherUtils
{
    private EasyMockMatcherUtils()
    {}

    /**
     * Typed 'is null' matcher.
     *
     * @param argumentType class representing argument's type
     * @param <T> type of the matched argument
     * @return typed 'is null' EasyMock matcher
     */
    public static <T> T nullArg(Class<T> argumentType)
    {
        notNull("argumentType", argumentType);
        return argumentType.cast(EasyMock.isNull());
    }

    /**
     * <p/>
     * Typed 'any' matcher.
     *
     * <p/>
     * WARNING this is just syntactic sugar, it is different
     * than {@link EasyMock#isA(Class)} in that it does not check type of the actual
     * argument.
     *
     * @param argumentType class representing argument's type
     * @param <T> type of the matched argument
     * @return typed 'any' EasyMock matcher
     */
    public static <T> T any(Class<T> argumentType)
    {
        notNull("argumentType", argumentType);
        return argumentType.cast(EasyMock.anyObject());
    }

    /**
     * Shortcut for {@link #any(Class)} called with String class.
     *
     * @return typed 'any string' EasyMock matcher
     * @see #any(Class)
     */
    public static String anyString()
    {
        return any(String.class);
    }


    /**
     * <p/>
     * Typed 'any map' matcher.
     *
     * <p/>
     * This is equivalent to {@link org.easymock.EasyMock#isA(Class)}} with a <tt>Map</tt> parameter,
     * but avoids type-safety warnings in the calling methods.
     *
     * @param keyType class representing the type of map keys
     * @param valueType class representing the type of map values
     * @param <K> type of the map key
     * @param <V> type of the map value
     * @return typed 'is any map' EasyMock matcher
     */
    @SuppressWarnings ( { "unchecked" })
    public static <K,V> Map<K,V> anyMap(Class<K> keyType, Class<V> valueType)
    {
        Assertions.notNull(keyType);
        Assertions.notNull(valueType);
        return (Map<K,V>) EasyMock.isA(Map.class);
    }

    /**
     * <p/>
     * Typed 'any list' matcher.
     *
     * <p/>
     * This is equivalent to {@link org.easymock.EasyMock#isA(Class)} with a <tt>List</tt> parameter,
     * but avoids type-safety warnings in the calling methods.
     *
     * @param elementType class representing the type of list elements
     * @param <E> type of the list element
     * @return typed 'is any list' EasyMock matcher
     */
    @SuppressWarnings ( { "unchecked" })
    public static <E> List<E> anyList(Class<E> elementType)
    {
        Assertions.notNull(elementType);
        return (List<E>) EasyMock.isA(List.class);
    }

    /**
     * <p/>
     * Typed 'any collection' matcher.
     *
     * <p/>
     * This is equivalent to {@link org.easymock.EasyMock#isA(Class)}, with a <tt>Collection</tt> parameter, but
     * avoids type-safety warnings in the calling methods.
     *
     * @param elementType class representing the type of list elements
     * @param <E> type of the collection element
     * @return typed 'is any collection' EasyMock matcher
     */
    @SuppressWarnings ( { "unchecked" })
    public static <E> Collection<E> anyCollection(Class<E> elementType)
    {
        Assertions.notNull(elementType);
        return (Collection<E>) EasyMock.isA(Collection.class);
    }

    /**
     * Matcher for a map containing given <tt>key</tt> and <tt>value</tt>.
     *
     * @param key key
     * @param value value
     * @param <K> type of key
     * @param <V> type of value
     * @return map containing matcher
     */
    public static <K,V> Map<K,V> mapContaining(K key, V value)
    {
        EasyMock.reportMatcher(MapKeyValueMatcher.forPair(key, value));
        return null;
    }

    /**
     * Matcher for a map containing at least two entries specified by given keys and values
     *
     * @param key1 first key
     * @param value1 first value
     * @param key2 second key
     * @param value2 second value
     * @param <K> type of key
     * @param <V> type of value
     * @return map containing matcher
     */
    public static <K,V> Map<K,V> mapContaining(K key1, V value1, K key2, V value2)
    {
        EasyMock.reportMatcher(MapKeyValueMatcher.forPairs(key1, value1, key2, value2));
        return null;
    }
}
