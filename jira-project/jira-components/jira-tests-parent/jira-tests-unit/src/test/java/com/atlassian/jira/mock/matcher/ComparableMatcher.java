package com.atlassian.jira.mock.matcher;

/**
 * A {@link com.atlassian.jira.mock.matcher.ArgumentMatcher} that uses the {@link Comparable} interface to see
 * if the objects are equal.
 *
 * @since v4.0
 */
final class ComparableMatcher<T extends Comparable<? super T>> implements ArgumentMatcher<T>
{
    private static final ComparableMatcher instance = new ComparableMatcher();

    private ComparableMatcher()
    {
    }

    public boolean match(final T expected, final T acutal)
    {
        if (expected == acutal)
        {
            return true;
        }
        else if (expected == null || acutal == null)
        {
            return false;
        }
        else
        {
            return expected.compareTo(acutal) == 0;
        }
    }

    public String toString(final T object)
    {
        return String.valueOf(object);
    }

    @SuppressWarnings("unchecked")
    static <T extends Comparable<? super T>> ComparableMatcher<T> comparableMatcher()
    {
        //we can do this cast since we know that ((T)t1).compareTo((T)t2) will work.
        return (ComparableMatcher<T>) instance;
    }

    @Override
    public String toString()
    {
        return "Comparable Argument Matcher";
    }
}
