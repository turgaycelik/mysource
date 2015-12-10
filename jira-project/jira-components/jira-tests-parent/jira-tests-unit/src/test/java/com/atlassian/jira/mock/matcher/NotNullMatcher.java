package com.atlassian.jira.mock.matcher;

/**
 * Matcher that ensures that the actual argument is not null.
 *
 * @since v4.0
 */
class NotNullMatcher implements ArgumentMatcher<Object>
{
    private final static NotNullMatcher instance = new NotNullMatcher();

    public boolean match(final Object expected, final Object acutal)
    {
        return acutal != null;
    }

    public String toString(final Object object)
    {
        return String.valueOf(object);
    }

    @Override
    public String toString()
    {
        return "[Not Null Argument Matcher]";
    }

    @SuppressWarnings ("unchecked")
    static <T> ArgumentMatcher<T> notNullMatcher()
    {
        //We can do this cast because we always treat any arguments as objects.
        return (ArgumentMatcher<T>) instance;
    }
}
