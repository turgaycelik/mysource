package com.atlassian.jira.mock.matcher;

/**
 * Simple matcher that always returns true. Mainly useful when you don't care what the argument is.
 *
 * @since v4.0
 */
class AlwaysMatcher implements ArgumentMatcher<Object>
{
    private static final AlwaysMatcher instance = new AlwaysMatcher();

    private AlwaysMatcher()
    {
    }

    public boolean match(final Object expected, final Object acutal)
    {
        return true;
    }

    public String toString(final Object object)
    {
        return String.valueOf(object);
    }

    @SuppressWarnings("unchecked")
    static <T>ArgumentMatcher<T> alwaysMatcher()
    {
        //We can do this cast because we always treat any arguments as objects.
        return (ArgumentMatcher<T>) instance;
    }

    @Override
    public String toString()
    {
        return "Always Argument Matcher";
    }
}
