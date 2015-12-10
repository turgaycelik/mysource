package com.atlassian.jira.task;

/**
 * Class to represent a condition when searching for tasks.
 *
 * @since v3.13
 */

public interface TaskMatcher
{
    /**
     * Tells the caller whether the passed descriptor matches or not.
     *
     * @param descriptor the descriptor to test.
     * @return true if the passed descriptor "matches" or false otherwise.
     */
    boolean match(TaskDescriptor<?> descriptor);
}
