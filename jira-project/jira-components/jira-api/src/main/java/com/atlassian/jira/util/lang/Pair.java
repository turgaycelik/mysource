package com.atlassian.jira.util.lang;

import javax.annotation.Nonnull;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.annotation.Nullable;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Immutable, generic pair of values.
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

    /**
     * By default we create a strict pair of non-null values.
     *
     * @param first first value
     * @param second second value
     * @param <U> type of first value
     * @param <V> type of second value
     * @return new pair
     * @see #strictPairOf(Object, Object)
     */
    public static <U,V> Pair<U,V> of(U first, V second)
    {
        return strictPairOf(first, second);
    }

    /**
     * A pair that doesn't allow <code>null</code> values.
     *
     * @param first first value, may not be <code>null</code>
     * @param second second value, may not be <code>null</code>
     * @param <U> type of first value
     * @param <V> type of second value
     * @return new strict pair
     */
    public static <U,V> Pair<U,V> strictPairOf(@Nonnull U first, @Nonnull V second)
    {
        return new Pair<U,V>(notNull("first", first), notNull("second", second));
    }

    /**
     * A pair that does allows <code>null</code> values.
     *
     * @param first first value, may be <code>null</code>
     * @param second second value, may be <code>null</code>
     * @param <U> type of first value
     * @param <V> type of second value
     * @return new nice pair
     */
    public static <U,V> Pair<U,V> nicePairOf(@Nullable U first, @Nullable V second)
    {
        return new Pair<U,V>(first, second);
    }

    private final F first;
    private final S second;

    private Pair(final F first, final S second)
    {
        this.first = first;
        this.second = second;
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
        return new HashCodeBuilder().append(this.first).append(this.second).toHashCode();
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
        return new EqualsBuilder().append(this.first, that.first).append(this.second, that.second).isEquals();
    }

    @Override
    public String toString()
    {
        return asString("Pair[", "first=", first, ", second=", second, "]");
    }
}
