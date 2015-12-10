package com.atlassian.jira.mock.matcher;

/**
 * Simple matcher that uses the natural ordering of objects to see if they are equal.
 *
 * @since v4.0
 */
class NaturalMatcher implements ArgumentMatcher<Object>
{
    private static final NaturalMatcher instance = new NaturalMatcher();

    private NaturalMatcher()
    {
    }

    public boolean match(final Object expected, final Object actual)
    {
        if (expected == actual)
        {
            return true;
        }
        else if (expected == null || actual == null)
        {
            return false;
        }
        else
        {
            return expected.equals(actual);
        }
    }

    public String toString(final Object object)
    {
        return String.valueOf(object);
    }

    @SuppressWarnings("unchecked")
    static <T> ArgumentMatcher<T> naturalMatcher()
    {
        //We can do this because this class only ever treats T as if it where an object.
        return (ArgumentMatcher<T>) instance;
    }

    @Override
    public String toString()
    {
        return "Natural Argument Matcher";
    }
}
