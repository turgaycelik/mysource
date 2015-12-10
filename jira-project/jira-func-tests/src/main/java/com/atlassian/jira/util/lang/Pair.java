package com.atlassian.jira.util.lang;

import net.jcip.annotations.ThreadSafe;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Immutable, generic pair of non-null values.
 * How coool is that!
 *
 * @param <F> type of the first element
 * @param <S> type of the second element
 *
 * @since v4.2
 */
@ThreadSafe
public final class Pair<F, S>
{
    public static <U,V> Pair<U,V> of(U first, V second)
    {
        return new Pair<U,V>(first, second);
    }

    private final F first;
    private final S second;



    private Pair(final F first, final S second) {
        this.first = notNull("first", first);
        this.second = notNull("second", second);
    }


    public F first()
    {
        return first;
    }

    public S second()
    {
        return second;
    }
    
    @Override
    public int hashCode()
    {
        return (first.hashCode() * 37) + second.hashCode();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final Pair that = (Pair) o;
        return this.first.equals(that.first) && this.second.equals(that.second);
    }

    @Override
    public String toString()
    {
        return asString("Pair[","first=", first,", second=",second,"]");
    }
}
