package com.atlassian.jira.easymock;

import com.atlassian.jira.util.lang.Pair;
import com.google.common.collect.ImmutableList;
import org.easymock.IArgumentMatcher;
import org.easymock.internal.matchers.InstanceOf;

import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.lang.Pair.nicePairOf;

/**
 * Matches maps containing given keys and values.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class MapKeyValueMatcher<K,V> implements IArgumentMatcher
{
    public static <L,M> MapKeyValueMatcher<L,M> forPair(L key, M value)
    {
        return new MapKeyValueMatcher<L,M>(nicePairOf(key, value));
    }

    public static <L,M> MapKeyValueMatcher<L,M> forPairs(L key1, M value1, L key2, M value2)
    {
        return new MapKeyValueMatcher<L,M>(nicePairOf(key1, value1), nicePairOf(key2, value2));
    }

    private final List<Pair<K,V>> thingsToMatch;
    private final IArgumentMatcher isAMapMatcher = new InstanceOf(Map.class); // <- type safety WIN!:D

    public MapKeyValueMatcher(List<Pair<K,V>> thingsToMatch)
    {
        this.thingsToMatch = thingsToMatch;
    }

    public MapKeyValueMatcher(Pair<K,V>...thingsToMatch)
    {
        this(ImmutableList.of(thingsToMatch));
    }

    @Override
    public boolean matches(Object argument)
    {
        if (!isAMapMatcher.matches(argument))
        {
            return false;
        }
        @SuppressWarnings("unchecked") final Map<K,V> map = (Map) argument;
        for (Pair<K,V> toMatch : thingsToMatch)
        {
            if (!map.containsKey(toMatch.first()))
            {
                return false;
            }
            if (toMatch.second() == null && map.get(toMatch.first()) != null)
            {
                return false;
            }
            if (toMatch.second() != null && !toMatch.second().equals(map.get(toMatch.first())))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void appendTo(StringBuffer buffer)
    {
        buffer.append("Map containing ").append(thingsToMatch);
    }
}
