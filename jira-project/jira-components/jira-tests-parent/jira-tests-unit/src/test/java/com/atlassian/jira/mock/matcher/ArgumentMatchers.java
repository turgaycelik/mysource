package com.atlassian.jira.mock.matcher;

import java.util.Comparator;

/**
 * An abstract factory for some simple assertions.
 *
 * @since v4.0
 */
public final class ArgumentMatchers
{
    private ArgumentMatchers()
    {
    }

    /**
     * Return a matcher that compares two objects using their {@link Object#equals(Object)}  method.
     *
     * @param <T> the type of the objects the matcher should work with.
     * @return the matcher.
     */
    public static <T> ArgumentMatcher<T> naturalMatcher()
    {
        return NaturalMatcher.naturalMatcher();
    }

    /**
     * Return a matcher that compares two objects using the passed comparator.
     *
     * @param comparatorMatcher the comparator to wrap.
     * @param <T> the type of the objects the matcher should workd with.
     * @return the matcher.
     */
    public static <T> ArgumentMatcher<T> comparatorMatcher(Comparator<? super T> comparatorMatcher)
    {
        return new ComparatorAdaptorMatcher<T>(comparatorMatcher);
    }

    /**
     * Return a matcher that compares two {@link Comparable} objects using the first's {@link
     * Comparable#compareTo(Object)}} method.
     *
     * @param <T> the type of the Comparable objects the matcher should work with.
     * @return the matcher.
     */
    @SuppressWarnings ({ "RedundantTypeArguments" })
    public static <T extends Comparable<? super T>> ArgumentMatcher<T> comparableMatcher()
    {
        return ComparableMatcher.<T>comparableMatcher();
    }

    /**
     * Return a matcher that always returns true.
     *
     * @param <T> the type of the objects the matcher should work with.
     * @return the matcher.
     */
    public static <T> ArgumentMatcher<T> alwaysMatcher()
    {
        return AlwaysMatcher.alwaysMatcher();
    }

    /**
     * Return a macther that returns true when the argument is not null.
     *
     * @param <T> the type of the object the matcher should work with.
     * @return the matcher.
     */
    public static <T> ArgumentMatcher<T> notNullMatcher()
    {
        return NotNullMatcher.notNullMatcher();
    }
}
