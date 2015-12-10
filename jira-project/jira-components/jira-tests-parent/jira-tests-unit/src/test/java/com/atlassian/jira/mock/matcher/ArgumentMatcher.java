package com.atlassian.jira.mock.matcher;

/**
 * Interface that can be used to compare an expected and actual argument to a unit test.
 *
 * @since v4.0
 * @param <T> the argument type being matched.
 */
public interface ArgumentMatcher<T>
{
    /**
     * Determines whether or not the passed two arguments are considered equal.
     *
     * @param expected the expected unit test object.
     * @param acutal the actual unit test object.
     * @return true if the passed arguments match, false otherwise.
     */
    boolean match(T expected, T acutal);

    /**
     * Return a string representation of the passed object.
     *
     * @param object the object to get the string representation of.
     * @return passed object's string representation.
     */
    String toString(T object);
}
